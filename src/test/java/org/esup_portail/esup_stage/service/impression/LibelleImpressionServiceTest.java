package org.esup_portail.esup_stage.service.impression;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Résolution des libellés d'impression : repli clé par clé sur le français,
 * priorité de la surcharge déposée par l'établissement, insensibilité à la casse du code langue.
 */
class LibelleImpressionServiceTest {

    private static final String CLE_OUI = "protectionSociale.oui.libelle";
    private static final String CLE_TEXTE = "protectionSociale.oui.texte";

    @TempDir
    Path dataDir;

    private LibelleImpressionService service;

    @BeforeEach
    void setUp() {
        AppliProperties appliProperties = new AppliProperties();
        appliProperties.setDataDir(dataDir.toString());
        service = new LibelleImpressionService(appliProperties);
    }

    private void deposerSurcharge(String code, String contenu) throws Exception {
        Path dossier = dataDir.resolve("i18n");
        Files.createDirectories(dossier);
        Files.write(dossier.resolve("impression_" + code + ".properties"), contenu.getBytes(StandardCharsets.UTF_8));
        service.viderCache(code);
    }

    @Test
    void sertLesLibellesFrancaisParDefaut() {
        Map<String, String> libelles = service.getLibelles("fr");

        assertThat(libelles.get(CLE_OUI)).isEqualTo("OUI");
        assertThat(libelles.get(CLE_TEXTE)).isEqualTo("cette protection s'ajoute au maintien, à l'étranger, des droits issus du droit français.");
    }

    @Test
    void sertLesLibellesAnglaisLivresParDefaut() {
        assertThat(service.getLibelles("en").get(CLE_OUI)).isEqualTo("YES");
        assertThat(service.getLibelles("en").get("commun.debut")).isEqualTo("Start");
    }

    @Test
    void replieSurLeFrancaisQuandLaLangueLivreeNestPasTraduite() {
        assertThat(service.getLibelles("it")).isEqualTo(service.getLibelles("fr"));
    }

    @Test
    void replieSurLeFrancaisQuandLaLangueEstInconnue() {
        assertThat(service.getLibelles("pt")).isEqualTo(service.getLibelles("fr"));
    }

    @Test
    void replieSurLeFrancaisQuandLeCodeEstAbsent() {
        assertThat(service.getLibelles(null)).isEqualTo(service.getLibelles("fr"));
        assertThat(service.getLibelles("  ")).isEqualTo(service.getLibelles("fr"));
    }

    @Test
    void normaliseLaCasseDuCodeLangue() {
        assertThat(service.getLibelles("FR")).isEqualTo(service.getLibelles("fr"));
    }

    @Test
    void laSurchargePrendLePasSurLeFichierLivre() throws Exception {
        deposerSurcharge("en", CLE_OUI + "=YES\n");

        assertThat(service.getLibelles("en").get(CLE_OUI)).isEqualTo("YES");
    }

    @Test
    void laSurchargePeutEtreParcellaire() throws Exception {
        deposerSurcharge("en", CLE_OUI + "=YES\n" + CLE_TEXTE + "=\n");

        Map<String, String> libelles = service.getLibelles("en");
        assertThat(libelles.get(CLE_OUI)).isEqualTo("YES");
        assertThat(libelles.get(CLE_TEXTE)).isEqualTo("this protection is in addition to the continued entitlement, abroad, to rights under French law.");
        assertThat(libelles.keySet()).isEqualTo(service.getClesAutorisees());
    }

    @Test
    void ignoreLesClesInconnuesDeLaSurcharge() throws Exception {
        deposerSurcharge("en", "cle.inconnue=valeur\n");

        assertThat(service.getLibelles("en")).doesNotContainKey("cle.inconnue");
    }

    @Test
    void litLesAccentsDeLaSurchargeEnUtf8() throws Exception {
        deposerSurcharge("es", CLE_TEXTE + "=Si no se marca ninguna casilla, se aplicará el 6.3-1.\n");

        assertThat(service.getLibelles("es").get(CLE_TEXTE)).isEqualTo("Si no se marca ninguna casilla, se aplicará el 6.3-1.");
    }

    @Test
    void lEcritureDUneSurchargeEstPriseEnCompteImmediatement() {
        assertThat(service.getLibelles("it").get(CLE_OUI)).isEqualTo("OUI");

        service.ecrireSurcharge("it", (CLE_OUI + "=SI\n").getBytes(StandardCharsets.UTF_8));

        assertThat(service.existeSurcharge("it")).isTrue();
        assertThat(service.getLibelles("it").get(CLE_OUI)).isEqualTo("SI");
    }

    @Test
    void laSuppressionDUneSurchargeRetablitLeComportementLivre() {
        service.ecrireSurcharge("it", (CLE_OUI + "=SI\n").getBytes(StandardCharsets.UTF_8));
        service.supprimerSurcharge("it");

        assertThat(service.existeSurcharge("it")).isFalse();
        assertThat(service.getLibelles("it").get(CLE_OUI)).isEqualTo("OUI");
    }

    @Test
    void compteLesLibellesEffectivementTraduits() {
        int total = service.getClesAutorisees().size();
        assertThat(service.compterClesTraduites("fr")).isEqualTo(total);
        assertThat(service.compterClesTraduites("en")).isEqualTo(total);
        assertThat(service.compterClesTraduites("it")).isZero();

        service.ecrireSurcharge("it", (CLE_OUI + "=SI\n" + CLE_TEXTE + "=\n").getBytes(StandardCharsets.UTF_8));

        assertThat(service.compterClesTraduites("it")).isEqualTo(1);
    }

    @Test
    void proposeUnFichierDeTravailCompletMemeSansSurcharge() {
        String contenu = new String(service.getContenuFichierTravail("en"), StandardCharsets.UTF_8);

        assertThat(contenu).contains(CLE_OUI).contains(CLE_TEXTE);
    }

    @Test
    void proposeLaSurchargeCommeFichierDeTravailQuandElleExiste() {
        service.ecrireSurcharge("en", (CLE_OUI + "=YES\n").getBytes(StandardCharsets.UTF_8));

        assertThat(new String(service.getContenuFichierTravail("en"), StandardCharsets.UTF_8)).isEqualTo(CLE_OUI + "=YES\n");
    }

    @Test
    void proposeLeFichierFrancaisPourUneLangueNonLivree() {
        String contenu = new String(service.getContenuFichierTravail("pt"), StandardCharsets.UTF_8);

        assertThat(contenu).contains("protectionSociale.oui.libelle=OUI");
    }

    /**
     * Toutes les langues livrées doivent exposer exactement les clés du fichier français :
     * une clé oubliée dans une langue passerait inaperçue et resterait figée en français.
     */
    @Test
    void lesFichiersLivresExposentLesMemesCles() throws Exception {
        Set<String> clesReference = lireCles("fr");

        for (String code : new String[]{"en", "es", "al", "it", "fo"}) {
            assertThat(lireCles(code))
                    .as("clés du fichier impression_%s.properties", code)
                    .isEqualTo(clesReference);
        }
    }

    private Set<String> lireCles(String code) throws Exception {
        Properties proprietes = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/i18n/impression_" + code + ".properties")) {
            proprietes.load(new java.io.InputStreamReader(Objects.requireNonNull(is), StandardCharsets.UTF_8));
        }
        return proprietes.stringPropertyNames();
    }
}
