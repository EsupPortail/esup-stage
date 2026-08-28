package org.esup_portail.esup_stage.service.apitoken;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.ApiToken;
import org.esup_portail.esup_stage.model.AppProperty;
import org.esup_portail.esup_stage.repository.ApiTokenJpaRepository;
import org.esup_portail.esup_stage.repository.AppPropertyJpaRepository;
import org.esup_portail.esup_stage.service.proprety.PropertyCryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiTokenServiceTest {

    private static final String CLE_16_OCTETS =
            Base64.getEncoder().encodeToString("0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    private ApiTokenJpaRepository apiTokenJpaRepository;
    private AppPropertyJpaRepository appPropertyJpaRepository;
    private PropertyCryptoService propertyCryptoService;
    private ApiTokenService service;

    /** Séquence d'identifiants simulant l'auto-incrément, pour que reveal(id) retrouve le token. */
    private final List<ApiToken> enBase = new ArrayList<>();

    @BeforeEach
    void setUp() {
        apiTokenJpaRepository = mock(ApiTokenJpaRepository.class);
        appPropertyJpaRepository = mock(AppPropertyJpaRepository.class);

        AppliProperties appliProperties = new AppliProperties();
        appliProperties.setConfigEncryptionKey(CLE_16_OCTETS);
        propertyCryptoService = new PropertyCryptoService(appliProperties);

        service = new ApiTokenService();
        ReflectionTestUtils.setField(service, "apiTokenJpaRepository", apiTokenJpaRepository);
        ReflectionTestUtils.setField(service, "appPropertyJpaRepository", appPropertyJpaRepository);
        ReflectionTestUtils.setField(service, "propertyCryptoService", propertyCryptoService);

        when(apiTokenJpaRepository.save(any(ApiToken.class))).thenAnswer(invocation -> {
            ApiToken token = invocation.getArgument(0);
            if (token.getId() == null) {
                token.setId(enBase.size() + 1);
                enBase.add(token);
            }
            return token;
        });
        when(apiTokenJpaRepository.findByNomApplication(anyString())).thenReturn(List.of());
    }

    private ApiToken tokenChiffre(int id, String nom, String nomApplication, String valeurEnClair, boolean actif) {
        ApiToken apiToken = new ApiToken();
        apiToken.setId(id);
        apiToken.setNom(nom);
        apiToken.setNomApplication(nomApplication);
        apiToken.setActif(actif);
        apiToken.setTokenEncrypted(propertyCryptoService.encrypt(valeurEnClair));
        return apiToken;
    }

    @Test
    void laCreationChiffreLaValeurEtNeStockeRienEnClair() {
        ApiToken cree = service.create("Token webhook", "Esup-Signature");

        assertThat(cree.getNom()).isEqualTo("Token webhook");
        assertThat(cree.getNomApplication()).isEqualTo("Esup-Signature");
        assertThat(cree.isActif()).isTrue();
        assertThat(cree.getDateCreation()).isNotNull();
        assertThat(cree.getTokenEncrypted()).isNotBlank();

        // La valeur en base est bien chiffrée et se relit avec la clé de configuration
        String enClair = propertyCryptoService.decrypt(cree.getTokenEncrypted());
        assertThat(enClair).isNotBlank().isNotEqualTo(cree.getTokenEncrypted());
        // 32 octets encodés en Base64 URL sans padding
        assertThat(enClair).hasSize(43);
    }

    @Test
    void deuxTokensNOntJamaisLaMemeValeur() {
        ApiToken premier = service.create("Token A", "Application A");
        ApiToken second = service.create("Token B", "Application B");

        assertThat(propertyCryptoService.decrypt(premier.getTokenEncrypted()))
                .isNotEqualTo(propertyCryptoService.decrypt(second.getTokenEncrypted()));
    }

    @Test
    void unNomDApplicationDejaPrisEstRefuse() {
        when(apiTokenJpaRepository.findByNomApplication("Esup-Signature"))
                .thenReturn(List.of(tokenChiffre(1, "Existant", "Esup-Signature", "abc", true)));

        assertThatThrownBy(() -> service.create("Doublon", "Esup-Signature"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(apiTokenJpaRepository, never()).save(any(ApiToken.class));
    }

    @Test
    void leNomEtLApplicationSontObligatoires() {
        assertThatThrownBy(() -> service.create("  ", "Application"))
                .isInstanceOf(AppException.class);
        assertThatThrownBy(() -> service.create("Nom", null))
                .isInstanceOf(AppException.class);
    }

    @Test
    void authentifieUnTokenActif() {
        ApiToken actif = tokenChiffre(1, "Token webhook", "Esup-Signature", "valeur-secrete", true);
        when(apiTokenJpaRepository.findByActifTrue()).thenReturn(List.of(actif));

        Optional<ApiToken> resultat = service.authenticate("valeur-secrete");

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getNomApplication()).isEqualTo("Esup-Signature");
    }

    @Test
    void rejetteUnTokenInconnu() {
        when(apiTokenJpaRepository.findByActifTrue())
                .thenReturn(List.of(tokenChiffre(1, "Token", "Application", "valeur-secrete", true)));

        assertThat(service.authenticate("autre-valeur")).isEmpty();
    }

    /**
     * Un token désactivé n'est pas renvoyé par findByActifTrue : il est donc indiscernable
     * d'un token supprimé ou inconnu, conformément au message d'erreur unique.
     */
    @Test
    void rejetteUnTokenDesactiveCommeUnTokenInconnu() {
        when(apiTokenJpaRepository.findByActifTrue()).thenReturn(List.of());

        assertThat(service.authenticate("valeur-secrete")).isEmpty();
        assertThat(service.authenticate("valeur-inconnue")).isEmpty();
    }

    @Test
    void rejetteUneValeurVideOuNulle() {
        assertThat(service.authenticate(null)).isEmpty();
        assertThat(service.authenticate("   ")).isEmpty();
        verify(apiTokenJpaRepository, never()).findByActifTrue();
    }

    @Test
    void leRenouvellementRemplaceLaValeurSurLaMemeLigne() {
        ApiToken existant = tokenChiffre(1, "Token", "Application", "ancienne-valeur", true);
        when(apiTokenJpaRepository.findById(1)).thenReturn(Optional.of(existant));

        ApiToken renouvele = service.renew(1);

        assertThat(renouvele.getId()).isEqualTo(1);
        assertThat(propertyCryptoService.decrypt(renouvele.getTokenEncrypted())).isNotEqualTo("ancienne-valeur");
        assertThat(renouvele.getDateModification()).isNotNull();
    }

    @Test
    void laDesactivationTraceLAuteurEtLaDate() {
        ApiToken existant = tokenChiffre(1, "Token", "Application", "valeur", true);
        when(apiTokenJpaRepository.findById(1)).thenReturn(Optional.of(existant));

        ApiToken desactive = service.setActif(1, false);

        assertThat(desactive.isActif()).isFalse();
        assertThat(desactive.getDateModification()).isNotNull();
        assertThat(desactive.getLoginModification()).isNotNull();
    }

    @Test
    void revelerRenvoieLaValeurEnClair() {
        ApiToken existant = tokenChiffre(1, "Token", "Application", "valeur-secrete", true);
        when(apiTokenJpaRepository.findById(1)).thenReturn(Optional.of(existant));

        assertThat(service.reveal(1)).isEqualTo("valeur-secrete");
    }

    @Test
    void unIdentifiantInconnuRenvoieUne404() {
        when(apiTokenJpaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void laMigrationRepriseLesAnciensTokensPuisSupprimeLaPropriete() {
        AppProperty legacy = new AppProperty();
        legacy.setKey("appli.tokens");
        legacy.setValue("token-a;token-b");
        when(appPropertyJpaRepository.findByKey("appli.tokens")).thenReturn(legacy);

        service.migrateLegacyTokens();

        assertThat(enBase).hasSize(2);
        assertThat(enBase).extracting(ApiToken::getLoginCreation).containsOnly("(migration)");
        // Les anciennes valeurs restent valides, désormais chiffrées
        assertThat(enBase).extracting(t -> propertyCryptoService.decrypt(t.getTokenEncrypted()))
                .containsExactlyInAnyOrder("token-a", "token-b");
        verify(appPropertyJpaRepository).delete(legacy);
    }

    @Test
    void laMigrationEstIdempotenteSansLaPropriete() {
        when(appPropertyJpaRepository.findByKey("appli.tokens")).thenReturn(null);

        service.migrateLegacyTokens();

        assertThat(enBase).isEmpty();
        verify(apiTokenJpaRepository, never()).save(any(ApiToken.class));
    }

    @Test
    void leTokenInterneEstCreeALaPremiereUtilisation() {
        when(apiTokenJpaRepository.findByNomApplicationAndActifTrue(ApiTokenService.APPLICATION_INTERNE))
                .thenReturn(List.of());
        when(apiTokenJpaRepository.findById(1)).thenAnswer(i -> Optional.of(enBase.get(0)));

        String token = service.getInternalToken();

        assertThat(token).isNotBlank();
        assertThat(enBase).hasSize(1);
        assertThat(enBase.get(0).getNomApplication()).isEqualTo(ApiTokenService.APPLICATION_INTERNE);
    }

    @Test
    void leTokenInterneDesactiveEchoueExplicitement() {
        when(apiTokenJpaRepository.findByNomApplicationAndActifTrue(ApiTokenService.APPLICATION_INTERNE))
                .thenReturn(List.of());
        when(apiTokenJpaRepository.findByNomApplication(ApiTokenService.APPLICATION_INTERNE))
                .thenReturn(List.of(tokenChiffre(1, "Token interne", ApiTokenService.APPLICATION_INTERNE, "valeur", false)));

        assertThatThrownBy(() -> service.getInternalToken())
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }
}
