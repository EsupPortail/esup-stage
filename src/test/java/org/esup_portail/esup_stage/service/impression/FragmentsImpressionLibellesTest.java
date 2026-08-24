package org.esup_portail.esup_stage.service.impression;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fragments HTML que l'application injecte dans le texte de la convention ou de l'avenant :
 * leurs libellés viennent du contexte d'impression pour suivre la langue du document.
 * <p>
 * En production FreeMarker est configuré en mode « classic compatible » : une clé absente
 * s'affiche comme une chaîne vide, sans erreur. Une clé mal orthographiée dans un fragment
 * disparaîtrait donc silencieusement du document imprimé, d'où le contrôle de couverture.
 */
class FragmentsImpressionLibellesTest {

    private static final Path DOSSIER_TEMPLATES = Path.of("src", "main", "resources", "templates");
    private static final Pattern CLE_LIBELLE = Pattern.compile("libelles\\[\"([^\"]+)\"]");

    @TempDir
    Path dataDir;

    private Configuration configuration;
    private Map<String, String> libellesFrancais;

    @BeforeEach
    void setup() {
        configuration = new Configuration(Configuration.VERSION_2_3_27);
        // même configuration qu'en production, cf. ImpressionService
        configuration.setClassicCompatible(true);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);

        AppliProperties appliProperties = new AppliProperties();
        appliProperties.setDataDir(dataDir.toString());
        libellesFrancais = new LibelleImpressionService(appliProperties).getLibelles("fr");
    }

    private String lireFragment(String nom) throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/templates/" + nom)) {
            return new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String render(String nom, Map<String, Object> modele, Map<String, String> libelles) throws Exception {
        Map<String, Object> complet = new HashMap<>(modele);
        complet.put("libelles", libelles);
        Template template = new Template(nom, new StringReader(lireFragment(nom)), configuration);
        StringWriter writer = new StringWriter();
        template.process(complet, writer);
        return writer.toString();
    }

    /**
     * Libellés factices : chaque clé renvoie un marqueur reconnaissable, ce qui permet de vérifier
     * qu'aucun texte français ne subsiste en dur dans le fragment.
     */
    private Map<String, String> libellesFactices() {
        Map<String, String> factices = new LinkedHashMap<>();
        libellesFrancais.keySet().forEach(cle -> factices.put(cle, "@@" + cle + "@@"));
        return factices;
    }

    private void assertAucunTexteFrancaisResiduel(String rendu) {
        // les libellés courts (« du », « au », « Fin ») se retrouvent trop facilement dans les
        // valeurs ou le balisage : on ne contrôle que les libellés discriminants
        List<String> discriminants = libellesFrancais.values().stream()
                .filter(valeur -> valeur.length() >= 8)
                .toList();
        assertThat(discriminants).isNotEmpty();
        for (String libelle : discriminants) {
            assertThat(rendu).as("libellé français résiduel : %s", libelle).doesNotContain(libelle);
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Couverture des clés
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Test
    void toutesLesClesUtiliseesParLesTemplatesSontDefinies() throws Exception {
        Set<String> clesUtilisees = new LinkedHashSet<>();
        try (Stream<Path> templates = Files.list(DOSSIER_TEMPLATES)) {
            for (Path template : templates.filter(p -> p.toString().endsWith(".html")).toList()) {
                Matcher matcher = CLE_LIBELLE.matcher(Files.readString(template, StandardCharsets.UTF_8));
                while (matcher.find()) {
                    clesUtilisees.add(matcher.group(1));
                }
            }
        }

        assertThat(clesUtilisees).isNotEmpty();
        assertThat(libellesFrancais.keySet()).containsAll(clesUtilisees);
    }

    /**
     * Une clé définie mais que plus aucun template n'utilise ferait traduire du texte inutile.
     */
    @Test
    void toutesLesClesDefiniesSontUtiliseesParUnTemplate() throws Exception {
        Set<String> clesUtilisees = new LinkedHashSet<>();
        try (Stream<Path> templates = Files.list(DOSSIER_TEMPLATES)) {
            for (Path template : templates.filter(p -> p.toString().endsWith(".html")).toList()) {
                Matcher matcher = CLE_LIBELLE.matcher(Files.readString(template, StandardCharsets.UTF_8));
                while (matcher.find()) {
                    clesUtilisees.add(matcher.group(1));
                }
            }
        }

        assertThat(clesUtilisees).containsAll(libellesFrancais.keySet());
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Périodes d'interruption de stage
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private Map<String, Object> modeleAvecInterruptions() {
        Map<String, Object> periode = new HashMap<>();
        periode.put("dateDebutInterruption", "01/07/2026");
        periode.put("dateFinInterruption", "15/07/2026");
        Map<String, Object> convention = new HashMap<>();
        convention.put("periodesInterruptions", List.of(periode));
        return Map.of("convention", convention);
    }

    @Test
    void leTableauDesInterruptionsSuitLesLibelles() throws Exception {
        String rendu = render("template_convention_periodesInterruptions.html", modeleAvecInterruptions(), libellesFrancais);

        assertThat(rendu)
                .contains("Périodes d'interruptions de stage")
                .contains("Début")
                .contains("Fin")
                .contains("01/07/2026")
                .contains("15/07/2026");
    }

    @Test
    void leTableauDesInterruptionsNaPlusDeTexteEnDur() throws Exception {
        assertAucunTexteFrancaisResiduel(render("template_convention_periodesInterruptions.html", modeleAvecInterruptions(), libellesFactices()));
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Périodes de stage à horaires irréguliers
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private Map<String, Object> modeleAvecHorairesIrreguliers() {
        Map<String, Object> periode = new HashMap<>();
        periode.put("dateDebutPeriode", "01/03/2026");
        periode.put("dateFinPeriode", "30/06/2026");
        periode.put("nbHeuresJournalieres", 7);
        Map<String, Object> convention = new HashMap<>();
        convention.put("horaireIrregulier", List.of(periode));
        return Map.of("convention", convention);
    }

    @Test
    void leTableauDesHorairesIrreguliersSuitLesLibelles() throws Exception {
        String rendu = render("template_convention_horaireIrregulier.html", modeleAvecHorairesIrreguliers(), libellesFrancais);

        assertThat(rendu)
                .contains("Périodes de stage")
                .contains("Heures journalières")
                .contains("01/03/2026")
                .contains("30/06/2026");
    }

    @Test
    void leTableauDesHorairesIrreguliersNaPlusDeTexteEnDur() throws Exception {
        assertAucunTexteFrancaisResiduel(render("template_convention_horaireIrregulier.html", modeleAvecHorairesIrreguliers(), libellesFactices()));
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Motifs d'avenant
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private Map<String, Object> modeleAvenantTousMotifs() {
        Map<String, Object> interruption = new HashMap<>();
        interruption.put("dateDebutInterruption", "01/07/2026");
        interruption.put("dateFinInterruption", "15/07/2026");

        Map<String, Object> lieu = new HashMap<>();
        lieu.put("nom", "Service R&D");
        lieu.put("voie", "25 boulevard de la Tech");
        lieu.put("codePostal", "75002");
        lieu.put("commune", "Paris");
        lieu.put("paysLibelle", "France");

        Map<String, Object> personne = new HashMap<>();
        personne.put("nom", "Durand");
        personne.put("prenom", "Pierre");
        personne.put("civiliteLibelle", "M.");
        personne.put("fonction", "Tuteur");
        personne.put("tel", "0102030405");
        personne.put("mail", "pierre.durand@example.com");

        Map<String, Object> convention = new HashMap<>();
        convention.put("sujetStage", "Sujet initial");
        convention.put("dateDebutStage", "01/03/2026");
        convention.put("dateFinStage", "30/06/2026");
        convention.put("periodesInterruptions", List.of(interruption));
        convention.put("montantGratification", "500");
        convention.put("uniteGratificationLibelle", "EUR");
        convention.put("uniteDureeGratificationLibelle", "mois");
        convention.put("deviseGratification", "EUR");
        convention.put("modeVersGratificationLibelle", "Virement");

        Map<String, Object> avenant = new HashMap<>();
        avenant.put("rupture", true);
        avenant.put("dateRupture", "15/05/2026");
        avenant.put("commentaireRupture", "Rupture à l'amiable");
        avenant.put("modificationSujet", true);
        avenant.put("sujetStage", "Nouveau sujet");
        avenant.put("modificationPeriode", true);
        avenant.put("dateDebutStage", "01/04/2026");
        avenant.put("dateFinStage", "31/07/2026");
        avenant.put("periodesInterruptions", List.of(interruption));
        avenant.put("modificationMontantGratification", true);
        avenant.put("montantGratification", "600");
        avenant.put("uniteGratificationLibelle", "EUR");
        avenant.put("uniteDureeGratificationLibelle", "mois");
        avenant.put("deviseGratification", "EUR");
        avenant.put("modeVersGratificationLibelle", "Chèque");
        avenant.put("modificationLieu", true);
        avenant.put("service", lieu);
        avenant.put("modificationSalarie", true);
        avenant.put("contact", personne);
        avenant.put("modificationEnseignant", true);
        avenant.put("enseignant", personne);
        avenant.put("motifAvenant", "Autre motif");

        Map<String, Object> modele = new HashMap<>();
        modele.put("convention", convention);
        modele.put("avenant", avenant);
        modele.put("service", lieu);
        modele.put("contact", personne);
        modele.put("enseignant", personne);
        return modele;
    }

    @Test
    void lesMotifsDAvenantSuiventLesLibelles() throws Exception {
        String rendu = render("template_avenant_motifs.html", modeleAvenantTousMotifs(), libellesFrancais);

        assertThat(rendu)
                .contains("Rupture de stage")
                .contains("Modification du sujet de stage")
                .contains("Modification de la période de stage")
                .contains("Modification du montant de la gratification")
                .contains("Modification du lieu de stage")
                .contains("Modification du tuteur professionnel")
                .contains("Modification de l’enseignant référent")
                .contains("Autre modification");
        assertThat(rendu)
                .contains("Anciennes valeurs :")
                .contains("Nouvelles valeurs :")
                .contains("Monnaie utilisée pour le paiement :")
                .contains("du 01/07/2026 au 15/07/2026");
        assertThat(rendu)
                .contains("Nouveau sujet")
                .contains("pierre.durand@example.com");
    }

    @Test
    void lesMotifsDAvenantNontPlusDeTexteEnDur() throws Exception {
        assertAucunTexteFrancaisResiduel(render("template_avenant_motifs.html", modeleAvenantTousMotifs(), libellesFactices()));
    }

    @Test
    void afficheNeantQuandLaGratificationNestPasRenseignee() throws Exception {
        Map<String, Object> modele = new HashMap<>(modeleAvenantTousMotifs());
        Map<String, Object> convention = new HashMap<>((Map<String, Object>) modele.get("convention"));
        convention.remove("montantGratification");
        convention.remove("deviseGratification");
        convention.remove("modeVersGratificationLibelle");
        modele.put("convention", convention);

        assertThat(render("template_avenant_motifs.html", modele, libellesFrancais)).contains("Néant");
    }
}
