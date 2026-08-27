package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto;
import org.esup_portail.esup_stage.dto.ConventionEvaluationTuteurDto;
import org.esup_portail.esup_stage.dto.ReponseEntrepriseFormDto;
import org.esup_portail.esup_stage.dto.ReponseSupplementaireFormDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.EvaluationJwtService;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.MailerService;
import org.esup_portail.esup_stage.service.evaluation.EvaluationService;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EvaluationTuteurControllerTest {

    private EvaluationTuteurController controller;
    private EvaluationJwtService jwt;
    private EvaluationService evaluationService;
    private QuestionSupplementaireJpaRepository questionSupplementaireJpaRepository;
    private ReponseEvaluationJpaRepository reponseEvaluationJpaRepository;
    private ReponseSupplementaireJpaRepository reponseSupplementaireJpaRepository;
    private ConventionJpaRepository conventionJpaRepository;
    private CentreGestionJpaRepository centreGestionJpaRepository;
    private ImpressionService impressionService;
    private MailerService mailerService;
    private AppConfigService appConfigService;

    @BeforeEach
    void setUp() {
        controller = new EvaluationTuteurController();
        jwt = mock(EvaluationJwtService.class);
        evaluationService = mock(EvaluationService.class);
        questionSupplementaireJpaRepository = mock(QuestionSupplementaireJpaRepository.class);
        reponseEvaluationJpaRepository = mock(ReponseEvaluationJpaRepository.class);
        reponseSupplementaireJpaRepository = mock(ReponseSupplementaireJpaRepository.class);
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        centreGestionJpaRepository = mock(CentreGestionJpaRepository.class);
        impressionService = mock(ImpressionService.class);
        mailerService = mock(MailerService.class);
        appConfigService = mock(AppConfigService.class);
        ReflectionTestUtils.setField(controller, "jwt", jwt);
        ReflectionTestUtils.setField(controller, "evaluationService", evaluationService);
        ReflectionTestUtils.setField(controller, "QSJpaRepository", questionSupplementaireJpaRepository);
        ReflectionTestUtils.setField(controller, "reponseEvaluationJpaRepository", reponseEvaluationJpaRepository);
        ReflectionTestUtils.setField(controller, "reponseSupplementaireJpaRepository", reponseSupplementaireJpaRepository);
        ReflectionTestUtils.setField(controller, "conventionJpaRepository", conventionJpaRepository);
        ReflectionTestUtils.setField(controller, "centreGestionJpaRepository", centreGestionJpaRepository);
        ReflectionTestUtils.setField(controller, "impressionService", impressionService);
        ReflectionTestUtils.setField(controller, "mailerService", mailerService);
        ReflectionTestUtils.setField(controller, "appConfigService", appConfigService);

        when(appConfigService.getConfigAlerteMail()).thenReturn(new ConfigAlerteMailDto());
        when(reponseEvaluationJpaRepository.saveAndFlush(any(ReponseEvaluation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reponseSupplementaireJpaRepository.saveAndFlush(any(ReponseSupplementaire.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Convention conventionEvaluee() {
        Convention convention = new Convention();
        convention.setId(42);
        convention.setContact(new Contact());
        convention.setEtudiant(new Etudiant());
        CentreGestion centreGestion = new CentreGestion();
        FicheEvaluation fiche = new FicheEvaluation();
        centreGestion.setFicheEvaluation(fiche);
        convention.setCentreGestion(centreGestion);
        return convention;
    }

    private EvaluationTuteurToken tokenPour(Convention convention) {
        EvaluationTuteurToken token = new EvaluationTuteurToken();
        token.setToken("TOK");
        token.setConvention(convention);
        token.setContact(convention.getContact());
        return token;
    }

    @Test
    void accessEvaluationPageRetourneLesDonneesDuToken() {
        Convention convention = conventionEvaluee();
        when(evaluationService.getToken("TOK")).thenReturn(tokenPour(convention));
        when(questionSupplementaireJpaRepository.findByFicheEvaluation(0)).thenReturn(List.of());

        ConventionEvaluationTuteurDto dto = controller.accessEvaluationPage("TOK");

        assertThat(dto).isNotNull();

        when(evaluationService.getToken("MAUVAIS")).thenReturn(null);
        assertThatThrownBy(() -> controller.accessEvaluationPage("MAUVAIS"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> controller.accessEvaluationPage(" "))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Token manquant");
    }

    @Test
    void lesReponsesEntrepriseSontCreesEtMisesAJour() {
        Convention convention = conventionEvaluee();
        when(evaluationService.validateToken("TOK")).thenReturn(tokenPour(convention));

        ReponseEvaluation reponse = new ReponseEvaluation();
        when(evaluationService.initReponseEvaluation(42)).thenReturn(reponse);
        assertThat(controller.createReponseEntreprise("TOK", 42, new ReponseEntrepriseFormDto())).isSameAs(reponse);
        verify(evaluationService).setReponseEvaluationEntrepriseData(eq(reponse), any(ReponseEntrepriseFormDto.class));

        when(reponseEvaluationJpaRepository.findByConvention(42)).thenReturn(reponse);
        assertThat(controller.updateReponseEntreprise("TOK", 42, new ReponseEntrepriseFormDto())).isSameAs(reponse);

        when(reponseEvaluationJpaRepository.findByConvention(42)).thenReturn(null);
        assertThatThrownBy(() -> controller.updateReponseEntreprise("TOK", 42, new ReponseEntrepriseFormDto()))
                .isInstanceOf(AppException.class);
    }

    @Test
    void lesReponsesSupplementairesSontCreesEtMisesAJour() {
        Convention convention = conventionEvaluee();
        when(evaluationService.validateToken("TOK")).thenReturn(tokenPour(convention));

        ReponseSupplementaire reponse = new ReponseSupplementaire();
        when(evaluationService.initReponseSupplementaire(42, 3)).thenReturn(reponse);
        assertThat(controller.createReponseSupplementaire("TOK", 42, 3, new ReponseSupplementaireFormDto())).isSameAs(reponse);

        when(reponseSupplementaireJpaRepository.findByQuestionAndConvention(42, 3)).thenReturn(reponse);
        assertThat(controller.updateReponseSupplementaire("TOK", 42, 3, new ReponseSupplementaireFormDto())).isSameAs(reponse);

        when(reponseSupplementaireJpaRepository.findByQuestionAndConvention(42, 3)).thenReturn(null);
        assertThatThrownBy(() -> controller.updateReponseSupplementaire("TOK", 42, 3, new ReponseSupplementaireFormDto()))
                .isInstanceOf(AppException.class);
    }

    @Test
    void unTokenDUneAutreConventionEstRefuse() {
        Convention autre = conventionEvaluee();
        autre.setId(43);
        when(evaluationService.validateToken("TOK")).thenReturn(tokenPour(autre));

        assertThatThrownBy(() -> controller.createReponseEntreprise("TOK", 42, new ReponseEntrepriseFormDto()))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        when(evaluationService.validateToken("ABSENT")).thenReturn(null);
        assertThatThrownBy(() -> controller.createReponseEntreprise("ABSENT", 42, new ReponseEntrepriseFormDto()))
                .isInstanceOf(AppException.class);
    }

    @Test
    void laValidationMarqueLeTokenEtNotifie() {
        Convention convention = conventionEvaluee();
        when(evaluationService.validateToken("TOK")).thenReturn(tokenPour(convention));
        ReponseEvaluation reponse = new ReponseEvaluation();
        when(reponseEvaluationJpaRepository.findByConvention(42)).thenReturn(reponse);
        when(evaluationService.validateAndUseToken("TOK")).thenReturn(tokenPour(convention));

        assertThat(controller.validate("TOK", 42, true)).isTrue();
        assertThat(reponse.getValidationEntreprise()).isTrue();
        verify(mailerService).sendValidationMail(eq(convention), any(), eq(TemplateMail.CODE_EVAL_TUTEUR_REMPLIE),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());

        // dévalidation : pas de mail
        assertThat(controller.validate("TOK", 42, false)).isTrue();
        verify(mailerService, org.mockito.Mockito.times(1)).sendValidationMail(any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());

        when(evaluationService.validateAndUseToken("TOK")).thenReturn(null);
        assertThatThrownBy(() -> controller.validate("TOK", 42, true)).isInstanceOf(AppException.class);

        when(reponseEvaluationJpaRepository.findByConvention(42)).thenReturn(null);
        assertThatThrownBy(() -> controller.validate("TOK", 42, true)).isInstanceOf(AppException.class);
    }

    @Test
    void lePdfCompleteLEtablissementDeReference() {
        Convention convention = conventionEvaluee();
        when(evaluationService.validateUsedToken("TOK")).thenReturn(tokenPour(convention));
        when(conventionJpaRepository.findById((Integer) 42)).thenReturn(Optional.of(convention));
        CentreGestion etablissement = new CentreGestion();
        etablissement.setNomCentre("Université");
        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(etablissement);

        assertThat(controller.generatePDF("TOK", 42).getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(convention.getNomEtabRef()).isEqualTo("Université");
        verify(impressionService).generateEvaluationPDF(eq(convention), any(), any(), eq(2));

        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(null);
        convention.setNomEtabRef(null);
        assertThatThrownBy(() -> controller.generatePDF("TOK", 42)).isInstanceOf(AppException.class);

        when(conventionJpaRepository.findById((Integer) 42)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> controller.generatePDF("TOK", 42)).isInstanceOf(AppException.class);
    }

    @Test
    void leRenouvellementRevoqueLeTokenEtRelanceLeTuteur() {
        Convention convention = conventionEvaluee();
        Contact contact = convention.getContact();
        contact.setMail("tuteur@entreprise.fr");
        EvaluationTuteurToken token = tokenPour(convention);
        when(evaluationService.getToken("TOK")).thenReturn(token);
        when(conventionJpaRepository.findById((Integer) 42)).thenReturn(Optional.of(convention));

        ReflectionTestUtils.invokeMethod(controller, "envoiMailRenouvellement", "TOK", 42);

        verify(evaluationService).revokeToken("TOK");
        verify(mailerService).sendAlerteValidation(eq("tuteur@entreprise.fr"), eq(convention), any(),
                any(Utilisateur.class), eq("RENOUVELLEMENT_EVAL_TUTEUR"));

        when(evaluationService.getToken("MAUVAIS")).thenReturn(null);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(controller, "envoiMailRenouvellement", "MAUVAIS", 42))
                .isInstanceOf(AppException.class);
    }
}
