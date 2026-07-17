package org.esup_portail.esup_stage.service.impression;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Le fragment de l'article 6.3 porte la logique FreeMarker que le template éditable
 * n'a pas le droit de contenir : il est donc testé isolément.
 */
class TemplateProtectionSocialeTest {

    private static final String FRAGMENT = "/templates/template_convention_protectionSociale.html";

    private Configuration configuration;

    @BeforeEach
    void setup() {
        configuration = new Configuration(Configuration.VERSION_2_3_27);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
    }

    private String render(Object protectionSociale) throws Exception {
        String source;
        try (InputStream is = getClass().getResourceAsStream(FRAGMENT)) {
            source = new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
        }

        Map<String, Object> convention = new HashMap<>();
        convention.put("protectionSocialeOrganismeAccueil", protectionSociale);

        Template template = new Template("protectionSociale", new StringReader(source), configuration);
        StringWriter writer = new StringWriter();
        template.process(Collections.singletonMap("convention", convention), writer);
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
}
