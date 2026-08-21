package org.esup_portail.esup_stage.service.impression;

import com.itextpdf.io.image.ImageData;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.QuestionEvaluationJpaRepository;
import org.esup_portail.esup_stage.repository.QuestionSupplementaireJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateConventionJpaRepository;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.FilenameSanitizerService;
import org.esup_portail.esup_stage.service.impression.context.ImpressionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImpressionServiceTest {

    private ImpressionService impressionService;
    private CentreGestionJpaRepository centreGestionJpaRepository;
    private TemplateConventionJpaRepository templateConventionJpaRepository;
    private PreviewConventionFactory previewConventionFactory;
    private QuestionSupplementaireJpaRepository questionSupplementaireJpaRepository;
    private QuestionEvaluationJpaRepository questionEvaluationJpaRepository;

    @BeforeEach
    void setup() {
        centreGestionJpaRepository = mock(CentreGestionJpaRepository.class);
        templateConventionJpaRepository = mock(TemplateConventionJpaRepository.class);
        previewConventionFactory = mock(PreviewConventionFactory.class);
        questionSupplementaireJpaRepository = mock(QuestionSupplementaireJpaRepository.class);
        questionEvaluationJpaRepository = mock(QuestionEvaluationJpaRepository.class);

        FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_27);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassicCompatible(true);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        freeMarkerConfigurer.setConfiguration(configuration);

        impressionService = new ImpressionService() {
            @Override
            public String getDefaultText(String templateName) {
                if ("/templates/template_style.html".equals(templateName)) {
                    return "<style></style>";
                }
                return "";
            }

            @Override
            public void generatePDF(String texte, String filename, ImageData imageData, ByteArrayOutputStream ou, boolean isEvaluation) {
                ou.writeBytes(texte.getBytes(StandardCharsets.UTF_8));
            }
        };
        ReflectionTestUtils.setField(impressionService, "centreGestionJpaRepository", centreGestionJpaRepository);
        ReflectionTestUtils.setField(impressionService, "templateConventionJpaRepository", templateConventionJpaRepository);
        ReflectionTestUtils.setField(impressionService, "previewConventionFactory", previewConventionFactory);
        ReflectionTestUtils.setField(impressionService, "questionSupplementaireJpaRepository", questionSupplementaireJpaRepository);
        ReflectionTestUtils.setField(impressionService, "questionEvaluationJpaRepository", questionEvaluationJpaRepository);
        ReflectionTestUtils.setField(impressionService, "freeMarkerConfigurer", freeMarkerConfigurer);
        ReflectionTestUtils.setField(impressionService, "filenameSanitizerService", new FilenameSanitizerService());
        AppConfigService appConfigService = mock(AppConfigService.class);
        when(appConfigService.getConfigGenerale()).thenReturn(new ConfigGeneraleDto());
        ReflectionTestUtils.setField(impressionService, "appConfigService", appConfigService);
    }

    @Test
    void generatePreviewPDFAllowsMissingConventionFields() {
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(1);

        TemplateConvention templateConvention = new TemplateConvention();
        templateConvention.setId(84);
        templateConvention.setTexte("<p>Representant : ${convention.nomSignataireComposante}</p>");

        when(centreGestionJpaRepository.findById(Integer.valueOf(1))).thenReturn(Optional.of(centreGestion));
        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(null);
        when(templateConventionJpaRepository.findById(Integer.valueOf(84))).thenReturn(Optional.of(templateConvention));
        when(previewConventionFactory.createPreviewContext(centreGestion, null)).thenReturn(new ImpressionContext());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        impressionService.generatePreviewPDF(1, outputStream, 84);

        assertThat(outputStream.size()).isGreaterThan(0);
    }

    @Test
    void manageIfElseConvertsLegacyTemplateMarkers() {
        String html = "Convention $IF (convention.creditECTS != '') $FI ECTS $ELSE Stage obligatoire $ENDIF";

        String result = impressionService.manageIfElse(html);

        assertThat(result).isEqualTo("Convention <#if (convention.creditECTS != '')> ECTS <#else> Stage obligatoire </#if>");
    }

    @Test
    void generateConventionAvenantPDFUsesFicheEvaluationWhenConfigured() {
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(10);
        centreGestion.setNomCentre("Centre");
        FicheEvaluation ficheEvaluation = new FicheEvaluation();
        ficheEvaluation.setId(99);
        centreGestion.setFicheEvaluation(ficheEvaluation);
        TemplateConvention templateConvention = templateConvention();
        Convention convention = convention(centreGestion);

        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(centreGestion);
        when(templateConventionJpaRepository.findByTypeAndLangue(1, "fr")).thenReturn(templateConvention);
        when(questionSupplementaireJpaRepository.findByFicheEvaluation(99)).thenReturn(List.of());
        when(questionEvaluationJpaRepository.findAll()).thenReturn(List.of());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        impressionService.generateConventionAvenantPDF(convention, null, outputStream, false);

        assertThat(outputStream.size()).isGreaterThan(0);
        verify(questionSupplementaireJpaRepository).findByFicheEvaluation(99);
    }

    @Test
    void generateConventionAvenantPDFAllowsMissingFicheEvaluation() {
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(10);
        centreGestion.setNomCentre("Centre");
        TemplateConvention templateConvention = templateConvention();
        Convention convention = convention(centreGestion);

        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(centreGestion);
        when(templateConventionJpaRepository.findByTypeAndLangue(1, "fr")).thenReturn(templateConvention);
        when(questionEvaluationJpaRepository.findAll()).thenReturn(List.of());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        assertThatCode(() -> impressionService.generateConventionAvenantPDF(convention, null, outputStream, false))
                .doesNotThrowAnyException();

        assertThat(outputStream.size()).isGreaterThan(0);
        verify(questionSupplementaireJpaRepository, never()).findByFicheEvaluation(anyInt());
    }

    private TemplateConvention templateConvention() {
        TemplateConvention templateConvention = new TemplateConvention();
        templateConvention.setId(1);
        templateConvention.setTexte("<p>Convention</p>");
        return templateConvention;
    }

    private Convention convention(CentreGestion centreGestion) {
        TypeConvention typeConvention = new TypeConvention();
        typeConvention.setId(1);
        typeConvention.setLibelle("Stage");

        LangueConvention langueConvention = new LangueConvention();
        langueConvention.setCode("fr");
        langueConvention.setLibelle("Francais");

        Etudiant etudiant = new Etudiant();
        etudiant.setPrenom("Alice");
        etudiant.setNom("Durand");

        Service service = new Service();
        Structure structure = new Structure();

        Convention convention = new Convention();
        convention.setId(7);
        convention.setTypeConvention(typeConvention);
        convention.setLangueConvention(langueConvention);
        convention.setCentreGestion(centreGestion);
        convention.setEtudiant(etudiant);
        convention.setService(service);
        convention.setStructure(structure);
        convention.setAdresseEtabRef("Adresse");
        convention.setNomEtabRef("Etablissement");
        convention.setNomenclature(new ConventionNomenclature());
        ReflectionTestUtils.setField(convention, "PeriodeStage", Collections.emptyList());
        return convention;
    }
}
