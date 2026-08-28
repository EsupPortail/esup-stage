package org.esup_portail.esup_stage.service.apitoken;

import jakarta.transaction.Transactional;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.ApiToken;
import org.esup_portail.esup_stage.model.AppProperty;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.ApiTokenJpaRepository;
import org.esup_portail.esup_stage.repository.AppPropertyJpaRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.service.proprety.PropertyCryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ApiTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiTokenService.class);

    /**
     * Message unique renvoyé quel que soit le motif du rejet (token inconnu, désactivé, supprimé,
     * mal formé ou absent) afin de ne donner aucune information exploitable à un appelant non autorisé.
     */
    public static final String MESSAGE_TOKEN_INVALIDE = "Token invalide";

    /** Application réservée à l'usage interne (webhook esup-signature s'appelant via /public). */
    public static final String APPLICATION_INTERNE = "Esup-Stage (interne)";

    /** Ancienne clé de configuration remplacée par la table ApiToken. */
    private static final String LEGACY_TOKENS_KEY = "appli.tokens";

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private ApiTokenJpaRepository apiTokenJpaRepository;

    @Autowired
    private AppPropertyJpaRepository appPropertyJpaRepository;

    @Autowired
    private PropertyCryptoService propertyCryptoService;

    public ApiToken getById(Integer id) {
        return apiTokenJpaRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Token non trouvé"));
    }

    @Transactional
    public ApiToken create(String nom, String nomApplication) {
        String libelle = requireText(nom, "Le nom du token est obligatoire");
        String application = requireText(nomApplication, "Le nom de l'application est obligatoire");
        checkApplicationDisponible(application, null);

        ApiToken apiToken = new ApiToken();
        apiToken.setNom(libelle);
        apiToken.setNomApplication(application);
        apiToken.setActif(true);
        apiToken.setDateCreation(new Date());
        apiToken.setLoginCreation(currentLogin());
        apiToken.setTokenEncrypted(propertyCryptoService.encrypt(generateToken()));
        ApiToken saved = apiTokenJpaRepository.save(apiToken);
        LOGGER.info("Création du token d'API \"{}\" pour l'application \"{}\" par {}", saved.getNom(), saved.getNomApplication(), saved.getLoginCreation());
        return saved;
    }

    @Transactional
    public ApiToken update(Integer id, String nom, String nomApplication) {
        ApiToken apiToken = getById(id);
        String application = requireText(nomApplication, "Le nom de l'application est obligatoire");
        checkApplicationDisponible(application, id);
        apiToken.setNom(requireText(nom, "Le nom du token est obligatoire"));
        apiToken.setNomApplication(application);
        return touchAndSave(apiToken);
    }

    /**
     * Régénère la valeur du token sur la même ligne : l'ancienne valeur cesse immédiatement
     * d'être acceptée.
     */
    @Transactional
    public ApiToken renew(Integer id) {
        ApiToken apiToken = getById(id);
        apiToken.setTokenEncrypted(propertyCryptoService.encrypt(generateToken()));
        ApiToken saved = touchAndSave(apiToken);
        LOGGER.info("Renouvellement du token d'API \"{}\" (application \"{}\") par {}", saved.getNom(), saved.getNomApplication(), saved.getLoginModification());
        return saved;
    }

    @Transactional
    public ApiToken setActif(Integer id, boolean actif) {
        ApiToken apiToken = getById(id);
        apiToken.setActif(actif);
        ApiToken saved = touchAndSave(apiToken);
        LOGGER.info("{} du token d'API \"{}\" (application \"{}\") par {}", actif ? "Activation" : "Désactivation", saved.getNom(), saved.getNomApplication(), saved.getLoginModification());
        return saved;
    }

    @Transactional
    public void delete(Integer id) {
        ApiToken apiToken = getById(id);
        apiTokenJpaRepository.delete(apiToken);
        LOGGER.info("Suppression du token d'API \"{}\" (application \"{}\") par {}", apiToken.getNom(), apiToken.getNomApplication(), currentLogin());
    }

    /**
     * Déchiffre la valeur du token pour permettre à un administrateur de la recopier.
     */
    public String reveal(Integer id) {
        ApiToken apiToken = getById(id);
        String token = decrypt(apiToken);
        if (token == null) {
            throw new AppException(HttpStatus.SERVICE_UNAVAILABLE, "Impossible de déchiffrer le token : vérifiez la clé appli.configEncryptionKey");
        }
        LOGGER.info("Consultation de la valeur du token d'API \"{}\" (application \"{}\") par {}", apiToken.getNom(), apiToken.getNomApplication(), currentLogin());
        return token;
    }

    /**
     * Recherche le token actif correspondant à la valeur présentée par un appelant de l'API publique.
     * Un token désactivé ou supprimé est traité exactement comme un token inconnu.
     */
    public Optional<ApiToken> authenticate(String tokenPresente) {
        if (!StringUtils.hasText(tokenPresente)) {
            return Optional.empty();
        }
        byte[] presente = tokenPresente.getBytes(StandardCharsets.UTF_8);
        ApiToken trouve = null;
        for (ApiToken apiToken : apiTokenJpaRepository.findByActifTrue()) {
            String enClair = decrypt(apiToken);
            if (enClair == null) {
                continue;
            }
            // Comparaison à temps constant, et parcours complet de la liste pour ne pas laisser
            // le temps de réponse trahir la position du token dans la table
            if (MessageDigest.isEqual(presente, enClair.getBytes(StandardCharsets.UTF_8)) && trouve == null) {
                trouve = apiToken;
            }
        }
        return Optional.ofNullable(trouve);
    }

    /**
     * Valeur en clair du token réservé aux appels que l'application passe à sa propre API publique.
     * Le token est créé à la première utilisation et reste gérable depuis l'écran d'administration.
     */
    @Transactional
    public String getInternalToken() {
        List<ApiToken> existants = apiTokenJpaRepository.findByNomApplicationAndActifTrue(APPLICATION_INTERNE);
        if (existants.isEmpty()) {
            if (!apiTokenJpaRepository.findByNomApplication(APPLICATION_INTERNE).isEmpty()) {
                throw new AppException(HttpStatus.SERVICE_UNAVAILABLE, "Le token interne « " + APPLICATION_INTERNE + " » est désactivé : réactivez-le dans les tokens d'API");
            }
            ApiToken cree = create("Token interne", APPLICATION_INTERNE);
            return reveal(cree.getId());
        }
        return reveal(existants.get(0).getId());
    }

    /**
     * Reprend les tokens de l'ancienne propriété {@code appli.tokens} (séparés par des ";")
     * sous forme de tokens en base, puis supprime la propriété devenue inutile.
     * Idempotent : sans la ligne de configuration, la méthode ne fait rien.
     */
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void migrateLegacyTokens() {
        AppProperty legacy = appPropertyJpaRepository.findByKey(LEGACY_TOKENS_KEY);
        if (legacy == null) {
            return;
        }
        String valeurs = StringUtils.hasText(legacy.getValue()) ? legacy.getValue() : propertyCryptoService.decrypt(legacy.getValueEncrypted());
        if (StringUtils.hasText(valeurs)) {
            int index = 0;
            for (String valeur : valeurs.split("[;,]")) {
                String token = valeur.trim();
                if (!StringUtils.hasText(token)) {
                    continue;
                }
                index++;
                ApiToken apiToken = new ApiToken();
                apiToken.setNom("Token migré " + index);
                apiToken.setNomApplication("Application inconnue " + index);
                apiToken.setActif(true);
                apiToken.setDateCreation(new Date());
                apiToken.setLoginCreation("(migration)");
                apiToken.setTokenEncrypted(propertyCryptoService.encrypt(token));
                apiTokenJpaRepository.save(apiToken);
            }
            if (index > 0) {
                LOGGER.info("Migration de {} token(s) de la propriété {} vers la table ApiToken : pensez à les renommer depuis l'écran des tokens d'API", index, LEGACY_TOKENS_KEY);
            }
        }
        appPropertyJpaRepository.delete(legacy);
    }

    private void checkApplicationDisponible(String nomApplication, Integer idCourant) {
        for (ApiToken existant : apiTokenJpaRepository.findByNomApplication(nomApplication)) {
            if (idCourant == null || !idCourant.equals(existant.getId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Un token existe déjà pour l'application « " + nomApplication + " »");
            }
        }
    }

    private ApiToken touchAndSave(ApiToken apiToken) {
        apiToken.setDateModification(new Date());
        apiToken.setLoginModification(currentLogin());
        return apiTokenJpaRepository.save(apiToken);
    }

    private String decrypt(ApiToken apiToken) {
        try {
            return propertyCryptoService.decrypt(apiToken.getTokenEncrypted());
        } catch (AppException e) {
            LOGGER.error("Token d'API \"{}\" illisible : {}", apiToken.getNom(), e.getMessage());
            return null;
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String requireText(String valeur, String message) {
        if (!StringUtils.hasText(valeur)) {
            throw new AppException(HttpStatus.BAD_REQUEST, message);
        }
        return valeur.trim();
    }

    private String currentLogin() {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        return utilisateur != null ? utilisateur.getLogin() : "(auto)";
    }
}
