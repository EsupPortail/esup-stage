package org.esup_portail.esup_stage.service.impression;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.service.impression.context.ImpressionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * L'article 13 « Protection des données personnelles » du template par défaut porte
 * l'adresse mail du DPO, paramétrée dans la configuration générale.
 */
class TemplateArticleRgpdTest {

    private static final String TEMPLATE = "/templates/template_default_convention.html";

    private Configuration configuration;

    @BeforeEach
    void setup() {
        configuration = new Configuration(Configuration.VERSION_2_3_27);
        configuration.setClassicCompatible(true);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
    }

    private String texteParDefaut() throws Exception {
        try (InputStream is = getClass().getResourceAsStream(TEMPLATE)) {
            return new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void leTemplateParDefautPorteLArticleRgpdEtRenumeroteLArticleSuivant() throws Exception {
        String texte = texteParDefaut();

        assertThat(texte).contains("Article 13 - Protection des données personnelles");
        assertThat(texte).contains("Article 14 - Droit applicable - Tribunaux compétents");
        assertThat(texte).doesNotContain("Article 13 - Droit applicable");
        assertThat(texte).contains("loi informatique et libertés du 6 janvier 1978");
        assertThat(texte).contains("${config.mailDpo}");
    }

    @Test
    void lAdresseDuDpoParametreeEstInjecteeDansLArticle() throws Exception {
        ConfigGeneraleDto configGenerale = new ConfigGeneraleDto();
        configGenerale.setMailDpo("dpo@univ-example.fr");
        ImpressionContext impressionContext = new ImpressionContext();
        impressionContext.setConfig(new ImpressionContext.ConfigContext(configGenerale));

        String rendu = render(texteParDefaut(), impressionContext);

        assertThat(rendu).contains("qu'il peut exercer auprès de : dpo@univ-example.fr");
        assertThat(rendu).doesNotContain("${config.mailDpo}");
    }

    @Test
    void lArticleResteImprimableQuandLeMailDuDpoNestPasParametre() throws Exception {
        ImpressionContext impressionContext = new ImpressionContext();
        impressionContext.setConfig(new ImpressionContext.ConfigContext(new ConfigGeneraleDto()));

        String rendu = render(texteParDefaut(), impressionContext);

        assertThat(rendu).contains("Article 13 - Protection des données personnelles");
        assertThat(rendu).contains("qu'il peut exercer auprès de : </span>");
    }

    private String render(String source, ImpressionContext impressionContext) throws Exception {
        Template template = new Template("conventionParDefaut", new StringReader(source), configuration);
        StringWriter writer = new StringWriter();
        template.process(impressionContext, writer);
        return writer.toString();
    }
}
