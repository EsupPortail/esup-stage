package org.esup_portail.esup_stage.service.impression;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Garde-fou : les templates d'évaluation PDF doivent être syntaxiquement valides FreeMarker
 * après le même prétraitement que {@link ImpressionService#generateEvaluationPDF}.
 */
class EvaluationTemplateParseTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "/templates/template_evaluation_etu.html",
            "/templates/template_evaluation_ens.html",
            "/templates/template_evaluation_tuteur.html"
    })
    void evaluationTemplatesParseLikeImpressionPipeline(String templatePath) throws Exception {
        String html = buildImpressionHtml(templatePath);

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassicCompatible(true);

        assertThatCode(() -> new Template("template_", html, cfg))
                .as("Parse FreeMarker de %s", templatePath)
                .doesNotThrowAnyException();
    }

    @Test
    void evaluationEtuTemplateProcessesWithMinimalContext() throws Exception {
        String html = buildImpressionHtml("/templates/template_evaluation_etu.html");

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassicCompatible(true);
        Template template = new Template("template_", html, cfg);

        Map<String, Object> root = minimalEvaluationContext();
        assertThatCode(() -> template.process(root, new StringWriter()))
                .doesNotThrowAnyException();
    }

    private static String buildImpressionHtml(String templatePath) throws Exception {
        String html = flattenClasspathResource("/templates/template_style.html");
        html = html.replace("__project_fonts_dir__", "/tmp/fonts/");
        html += flattenClasspathResource(templatePath);
        html = html.replace("<figure", "<div").replace("</figure>", "</div>");
        return manageIfElse(html);
    }

    /** Même aplatissement que {@link ImpressionService#getDefaultText(String)}. */
    private static String flattenClasspathResource(String resourcePath) throws Exception {
        InputStream in = Objects.requireNonNull(
                EvaluationTemplateParseTest.class.getResourceAsStream(resourcePath),
                "Resource manquante: " + resourcePath
        );
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /** Même substitution que {@link ImpressionService#manageIfElse(String)}. */
    private static String manageIfElse(String text) {
        return text.replace("$IF", "<#if")
                .replace("$EQUALS ", "==\"")
                .replace(" $FI", ">")
                .replace("$ELSE", "<#else>")
                .replace("??$", "??>")
                .replace("$ENDIF", "</#if>");
    }

    private static Map<String, Object> minimalEvaluationContext() {
        Map<String, Object> convention = new HashMap<>();
        convention.put("id", "1");
        convention.put("sujetStage", "Sujet test");
        convention.put("origineStageLibelle", "Candidature spontanée");

        Map<String, Object> reponse = new HashMap<>();
        reponse.put("schemaVersion", 2);

        Map<String, Object> ficheEvaluation = new HashMap<>();
        // Toutes les questions à false : seuls les blocs hors <#if fiche...> restent.
        ficheEvaluation.put("questionEtuI1", false);

        Map<String, Object> q = new HashMap<>();
        q.put("code", "ETUIII0");
        q.put("texte", "Sujet d'origine");

        Map<String, Object> root = new HashMap<>();
        root.put("convention", convention);
        root.put("reponse", reponse);
        root.put("ficheEvaluation", ficheEvaluation);
        root.put("questionEvaluations", List.of(q));
        return root;
    }
}
