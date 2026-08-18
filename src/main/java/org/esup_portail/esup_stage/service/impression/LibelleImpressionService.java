package org.esup_portail.esup_stage.service.impression;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.enums.FolderEnum;
import org.esup_portail.esup_stage.exception.AppException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Résolution des libellés que l'application insère elle-même dans les documents imprimés
 * (fragments injectés par {@link ImpressionService}), dans la langue de la convention.
 * <p>
 * La résolution se fait clé par clé, du plus spécifique au plus général :
 * <ol>
 *     <li>surcharge de l'établissement : {@code <dataDir>/i18n/impression_<code>.properties} ;</li>
 *     <li>fichier livré avec l'application : {@code /i18n/impression_<code>.properties} ;</li>
 *     <li>repli sur le français : {@code /i18n/impression_fr.properties}.</li>
 * </ol>
 * Une valeur absente ou vide fait descendre d'un cran : une traduction partielle reste exploitable.
 */
@Service
public class LibelleImpressionService {

    private static final Logger logger = LogManager.getLogger(LibelleImpressionService.class);

    public static final String LANGUE_DEFAUT = "fr";

    private static final String PREFIXE_FICHIER = "impression_";
    private static final String SUFFIXE_FICHIER = ".properties";
    private static final String DOSSIER_CLASSPATH = "/i18n/";

    private final AppliProperties appliProperties;

    private final Map<String, Map<String, String>> cache = new ConcurrentHashMap<>();

    public LibelleImpressionService(AppliProperties appliProperties) {
        this.appliProperties = appliProperties;
    }

    /**
     * Libellés à utiliser pour la langue demandée, toutes clés servies (repli français inclus).
     */
    public Map<String, String> getLibelles(String codeLangue) {
        return cache.computeIfAbsent(normaliserCode(codeLangue), this::chargerLibelles);
    }

    /**
     * Clés reconnues par l'application, définies par le fichier français de référence.
     */
    public Set<String> getClesAutorisees() {
        return getLibelles(LANGUE_DEFAUT).keySet();
    }

    public void viderCache(String codeLangue) {
        cache.remove(normaliserCode(codeLangue));
    }

    public void viderCache() {
        cache.clear();
    }

    public String getNomFichier(String codeLangue) {
        return PREFIXE_FICHIER + normaliserCode(codeLangue) + SUFFIXE_FICHIER;
    }

    public Path getCheminSurcharge(String codeLangue) {
        return Paths.get(appliProperties.getDataDir() + FolderEnum.LIBELLES_IMPRESSION + "/" + getNomFichier(codeLangue));
    }

    public boolean existeSurcharge(String codeLangue) {
        return Files.isRegularFile(getCheminSurcharge(codeLangue));
    }

    /**
     * Valeurs telles qu'écrites dans la surcharge de l'établissement, sans repli.
     * Map vide si aucune surcharge n'est déposée.
     */
    public Map<String, String> lireSurcharge(String codeLangue) {
        Path chemin = getCheminSurcharge(codeLangue);
        if (!Files.isRegularFile(chemin)) {
            return Collections.emptyMap();
        }
        try (Reader reader = Files.newBufferedReader(chemin, StandardCharsets.UTF_8)) {
            return versMap(lire(reader));
        } catch (Exception e) {
            logger.warn("Le fichier de libellés {} n'a pas pu être lu, il est ignoré", chemin, e);
            return Collections.emptyMap();
        }
    }

    /**
     * Valeurs telles que livrées avec l'application pour cette langue, sans repli.
     * Map vide si aucun fichier n'est livré pour la langue.
     */
    public Map<String, String> lireLivre(String codeLangue) {
        return versMap(lireProprietes(normaliserCode(codeLangue)));
    }

    /**
     * Nombre de libellés effectivement traduits dans la langue demandée, c'est-à-dire renseignés par
     * le fichier livré ou par la surcharge de l'établissement — les libellés servis par repli sur le
     * français ne sont pas comptés.
     */
    public int compterClesTraduites(String codeLangue) {
        String code = normaliserCode(codeLangue);
        Set<String> clesAutorisees = getClesAutorisees();
        if (LANGUE_DEFAUT.equals(code)) {
            return clesAutorisees.size();
        }
        Set<String> traduites = new LinkedHashSet<>();
        for (Map<String, String> source : List.of(lireLivre(code), lireSurcharge(code))) {
            for (Map.Entry<String, String> entree : source.entrySet()) {
                if (clesAutorisees.contains(entree.getKey()) && entree.getValue() != null && !entree.getValue().isBlank()) {
                    traduites.add(entree.getKey());
                }
            }
        }
        return traduites.size();
    }

    /**
     * Contenu à proposer en téléchargement pour servir de base de travail : la surcharge si elle
     * existe, sinon le fichier livré pour la langue, sinon le fichier français.
     */
    public byte[] getContenuFichierTravail(String codeLangue) {
        String code = normaliserCode(codeLangue);
        Path surcharge = getCheminSurcharge(code);
        if (Files.isRegularFile(surcharge)) {
            try {
                return Files.readAllBytes(surcharge);
            } catch (IOException e) {
                logger.warn("Le fichier de libellés {} n'a pas pu être lu", surcharge, e);
            }
        }
        byte[] livre = lireRessource(code);
        return livre != null ? livre : lireRessourceObligatoire(LANGUE_DEFAUT);
    }

    public void ecrireSurcharge(String codeLangue, byte[] contenu) {
        Path chemin = getCheminSurcharge(codeLangue);
        try {
            Files.createDirectories(chemin.getParent());
            Files.write(chemin, contenu);
        } catch (IOException e) {
            logger.error("Erreur lors de l'écriture du fichier de libellés {}", chemin, e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'enregistrement du fichier de libellés");
        }
        viderCache(codeLangue);
    }

    public void supprimerSurcharge(String codeLangue) {
        Path chemin = getCheminSurcharge(codeLangue);
        try {
            Files.deleteIfExists(chemin);
        } catch (IOException e) {
            logger.error("Erreur lors de la suppression du fichier de libellés {}", chemin, e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la suppression du fichier de libellés");
        }
        viderCache(codeLangue);
    }

    public Path getDossierSurcharge() {
        return Paths.get(appliProperties.getDataDir() + FolderEnum.LIBELLES_IMPRESSION);
    }

    private Map<String, String> chargerLibelles(String code) {
        Map<String, String> libelles = versMap(lireProprietesObligatoires(LANGUE_DEFAUT));
        if (!LANGUE_DEFAUT.equals(code)) {
            appliquer(libelles, versMap(lireProprietes(code)));
        }
        appliquer(libelles, lireSurcharge(code));
        return Collections.unmodifiableMap(libelles);
    }

    /**
     * Reporte les valeurs renseignées de {@code source} sur {@code cible}. Les valeurs vides et les
     * clés inconnues sont ignorées : la valeur déjà en place (langue livrée ou français) est conservée.
     */
    private void appliquer(Map<String, String> cible, Map<String, String> source) {
        for (Map.Entry<String, String> entree : source.entrySet()) {
            if (!cible.containsKey(entree.getKey())) {
                logger.warn("Libellé d'impression inconnu ignoré : {}", entree.getKey());
                continue;
            }
            if (entree.getValue() != null && !entree.getValue().isBlank()) {
                cible.put(entree.getKey(), entree.getValue().trim());
            }
        }
    }

    private Properties lireProprietesObligatoires(String code) {
        Properties proprietes = lireProprietes(code);
        if (proprietes == null) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Fichier de libellés d'impression " + getNomFichier(code) + " non trouvé");
        }
        return proprietes;
    }

    private Properties lireProprietes(String code) {
        try (InputStream is = getClass().getResourceAsStream(DOSSIER_CLASSPATH + getNomFichier(code))) {
            if (is == null) {
                return null;
            }
            return lire(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.error("Erreur lors de la lecture du fichier de libellés {}", getNomFichier(code), e);
            return null;
        }
    }

    private byte[] lireRessourceObligatoire(String code) {
        byte[] contenu = lireRessource(code);
        if (contenu == null) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Fichier de libellés d'impression " + getNomFichier(code) + " non trouvé");
        }
        return contenu;
    }

    private byte[] lireRessource(String code) {
        try (InputStream is = getClass().getResourceAsStream(DOSSIER_CLASSPATH + getNomFichier(code))) {
            return is == null ? null : is.readAllBytes();
        } catch (IOException e) {
            logger.error("Erreur lors de la lecture du fichier de libellés {}", getNomFichier(code), e);
            return null;
        }
    }

    private Properties lire(Reader reader) throws IOException {
        Properties proprietes = new Properties();
        proprietes.load(reader);
        return proprietes;
    }

    private Map<String, String> versMap(Properties proprietes) {
        Map<String, String> map = new LinkedHashMap<>();
        if (proprietes != null) {
            for (String cle : proprietes.stringPropertyNames()) {
                map.put(cle, proprietes.getProperty(cle));
            }
        }
        return map;
    }

    private String normaliserCode(String codeLangue) {
        if (codeLangue == null || codeLangue.isBlank()) {
            return LANGUE_DEFAUT;
        }
        return codeLangue.trim().toLowerCase(Locale.ROOT);
    }
}
