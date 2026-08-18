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
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Le fragment de l'article 6.3 porte la logique FreeMarker que le template éditable
 * n'a pas le droit de contenir : il est donc testé isolément.
 * Ses libellés proviennent du contexte d'impression, de façon à suivre la langue du document.
 */
class TemplateProtectionSocialeTest {

    private static final String FRAGMENT = "/templates/template_convention_protectionSociale.html";

    @TempDir
    Path dataDir;

    private Configuration configuration;

    @BeforeEach
    void setup() {
        configuration = new Configuration(Configuration.VERSION_2_3_27);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
    }

    private Map<String, String> libellesFrancais() {
        Map<String, String> libelles = new LinkedHashMap<>();
        libelles.put("protectionSociale.oui.libelle", "OUI");
        libelles.put("protectionSociale.oui.texte", "cette protection s'ajoute au maintien, à l'étranger, des droits issus du droit français.");
        libelles.put("protectionSociale.non.libelle", "NON");
        libelles.put("protectionSociale.non.texte", "la protection découle alors exclusivement du maintien, à l'étranger, des droits issus du régime français étudiant.");
        return libelles;
    }

    private Map<String, String> libellesAnglais() {
        Map<String, String> libelles = new LinkedHashMap<>();
        libelles.put("protectionSociale.oui.libelle", "YES");
        libelles.put("protectionSociale.oui.texte", "this cover is added to the maintenance abroad of the rights arising from French law.");
        libelles.put("protectionSociale.non.libelle", "NO");
        libelles.put("protectionSociale.non.texte", "the cover then arises exclusively from the maintenance abroad of the rights of the French student scheme.");
        return libelles;
    }

    private String render(Object protectionSociale) throws Exception {
        return render(protectionSociale, libellesFrancais());
    }

    private String render(Object protectionSociale, Map<String, String> libelles) throws Exception {
        String source;
        try (InputStream is = getClass().getResourceAsStream(FRAGMENT)) {
            source = new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
        }

        Map<String, Object> convention = new HashMap<>();
        convention.put("protectionSocialeOrganismeAccueil", protectionSociale);

        Map<String, Object> modele = new HashMap<>();
        modele.put("convention", convention);
        modele.put("libelles", libelles);

        Template template = new Template("protectionSociale", new StringReader(source), configuration);
        StringWriter writer = new StringWriter();
        template.process(modele, writer);
        return writer.toString();
    }

    @Test
    void cocheLaCaseOuiQuandLaProtectionEstFournie() throws Exception {
        String rendu = render(Boolean.TRUE);

        assertThat(rendu).contains("<span class=\"checkbox\">X</span>");
        assertThat(rendu.indexOf("<span class=\"checkbox\">X</span>")).isLessThan(rendu.indexOf("NON:"));
        assertThat(rendu).containsOnlyOnce("<span class=\"checkbox\">X</span>");
    }

    @Test
    void cocheLaCaseNonQuandLaProtectionNestPasFournie() throws Exception {
        String rendu = render(Boolean.FALSE);

        assertThat(rendu).containsOnlyOnce("<span class=\"checkbox\">X</span>");
        assertThat(rendu.indexOf("<span class=\"checkbox\">X</span>")).isGreaterThan(rendu.indexOf("OUI:"));
    }

    @Test
    void neCocheAucuneCaseQuandLeChampNestPasRenseigne() throws Exception {
        String rendu = render(null);

        assertThat(rendu).doesNotContain("<span class=\"checkbox\">X</span>");
        assertThat(rendu).contains("OUI:").contains("NON:");
    }

    /**
     * La phrase « Si aucune case n'est cochée, le 6.3-1 s'applique. » fait partie du texte du modèle
     * de convention depuis 2021, et se trouve donc déjà dans le texte saisi par les établissements.
     * La porter aussi dans le fragment l'imprimerait en double.
     */
    @Test
    void nePorteRienDeCeQueLeModeleContientDeja() throws Exception {
        String rendu = render(null);

        assertThat(rendu).doesNotContain("Si aucune case");
    }

    /**
     * En production FreeMarker est configuré en mode « classic compatible » : une clé absente
     * s'affiche comme une chaîne vide, sans erreur. Un libellé mal orthographié dans le fragment
     * disparaîtrait donc silencieusement de la convention imprimée.
     */
    @Test
    void toutesLesClesDuFragmentSontDefiniesDansLeFichierDeReference() throws Exception {
        String source;
        try (InputStream is = getClass().getResourceAsStream(FRAGMENT)) {
            source = new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
        }

        Set<String> clesUtilisees = new LinkedHashSet<>();
        Matcher matcher = Pattern.compile("libelles\\[\"([^\"]+)\"]").matcher(source);
        while (matcher.find()) {
            clesUtilisees.add(matcher.group(1));
        }

        AppliProperties appliProperties = new AppliProperties();
        appliProperties.setDataDir(dataDir.toString());
        Map<String, String> reference = new LibelleImpressionService(appliProperties).getLibelles("fr");

        assertThat(clesUtilisees).isNotEmpty();
        assertThat(reference.keySet()).containsAll(clesUtilisees);
    }

    @Test
    void seRendAvecLesLibellesFournisParLeService() throws Exception {
        AppliProperties appliProperties = new AppliProperties();
        appliProperties.setDataDir(dataDir.toString());
        Map<String, String> libelles = new LibelleImpressionService(appliProperties).getLibelles("fr");

        String rendu = render(Boolean.TRUE, libelles);

        assertThat(rendu)
                .contains("OUI:")
                .contains("NON:")
                .contains("cette protection s'ajoute au maintien")
                .contains("la protection découle alors exclusivement");
    }

    @Test
    void rendLeBlocDansLaLangueDesLibellesFournis() throws Exception {
        String rendu = render(Boolean.TRUE, libellesAnglais());

        assertThat(rendu)
                .contains("YES:")
                .contains("NO:")
                .contains("this cover is added to the maintenance abroad")
                .contains("the cover then arises exclusively");
        assertThat(rendu)
                .doesNotContain("OUI:")
                .doesNotContain("NON:")
                .doesNotContain("cette protection s'ajoute")
                .doesNotContain("la protection découle");
        assertThat(rendu).containsOnlyOnce("<span class=\"checkbox\">X</span>");
    }
}
