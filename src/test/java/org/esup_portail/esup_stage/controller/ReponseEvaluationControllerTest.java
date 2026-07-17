package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto;
import org.esup_portail.esup_stage.dto.ReponseEnseignantFormDto;
import org.esup_portail.esup_stage.dto.ReponseEntrepriseFormDto;
import org.esup_portail.esup_stage.dto.ReponseEtudiantFormDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.ReponseEvaluationJpaRepository;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ConventionService;
import org.esup_portail.esup_stage.service.MailerService;
import org.esup_portail.esup_stage.service.evaluation.EvaluationService;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReponseEvaluationControllerTest {

    private ReponseEvaluationController controller;
    private ReponseEvaluationJpaRepository reponseEvaluationJpaRepository;
    private ConventionJpaRepository conventionJpaRepository;
    private ImpressionService impressionService;
    private MailerService mailerService;
    private AppConfigService appConfigService;
    private EvaluationService evaluationService;
    private ConventionService conventionService;

    @BeforeEach
    void setUp() {
        controller = new ReponseEvaluationController();
        reponseEvaluationJpaRepository = mock(ReponseEvaluationJpaRepository.class);
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        impressionService = mock(ImpressionService.class);
        mailerService = mock(MailerService.class);
        appConfigService = mock(AppConfigService.class);
        evaluationService = mock(EvaluationService.class);
        conventionService = mock(ConventionService.class);
        controller.reponseEvaluationJpaRepository = reponseEvaluationJpaRepository;
        controller.conventionJpaRepository = conventionJpaRepository;
        controller.impressionService = impressionService;
        controller.mailerService = mailerService;
        controller.appConfigService = appConfigService;
        controller.evaluationService = evaluationService;
        controller.conventionService = conventionService;

        when(appConfigService.getConfigAlerteMail()).thenReturn(new ConfigAlerteMailDto());
        when(reponseEvaluationJpaRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void connecte(String uid) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUid(uid);
        utilisateur.setLogin(uid);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CasUserDetailsImpl(utilisateur, List.of()), null));
    }

    private ReponseEvaluation reponseAvecConvention() {
        ReponseEvaluation reponseEvaluation = new ReponseEvaluation();
        Convention convention = new Convention();
        convention.setId(42);
        reponseEvaluation.setConvention(convention);
        return reponseEvaluation;
    }

    // ------------------------------------------------------------------
    // getByConvention
    // ------------------------------------------------------------------

    @Test
    void getByConventionRenvoieLaReponseApresControleDAcces() {
        Convention convention = new Convention();
        ReponseEvaluation reponseEvaluation = new ReponseEvaluation();
        convention.setReponseEvaluation(reponseEvaluation);
        when(conventionJpaRepository.findById(42)).thenReturn(convention);

        assertThat(controller.getByConvention(42)).isSameAs(reponseEvaluation);
        verify(conventionService).canViewEditConvention(eq(convention), any());
    }

    @Test
    void getByConventionEchoueSiConventionInconnue() {
        when(conventionJpaRepository.findById(99)).thenReturn(null);

        assertThatThrownBy(() -> controller.getByConvention(99))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ------------------------------------------------------------------
    // création / mise à jour des réponses par profil
    // ------------------------------------------------------------------

    @Test
    void createReponseEtudiantValideEnvoieLeMail() {
        connecte("etu1");
        ReponseEvaluation reponseEvaluation = reponseAvecConvention();
        when(evaluationService.initReponseEvaluation(42)).thenReturn(reponseEvaluation);
        ReponseEtudiantFormDto form = new ReponseEtudiantFormDto();

        ReponseEvaluation resultat = controller.createReponseEtudiant(42, true, form);

        assertThat(resultat.getValidationEtudiant()).isTrue();
        verify(mailerService).sendValidationMail(eq(reponseEvaluation.getConvention()), any(), any(Utilisateur.class),
                eq(TemplateMail.CODE_EVAL_ETU_REMPLIE), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
        verify(evaluationService).setReponseEvaluationEtudiantData(reponseEvaluation, form);
        verify(reponseEvaluationJpaRepository).saveAndFlush(reponseEvaluation);
    }

    @Test
    void createReponseEtudiantNonValideNEnvoiePasDeMail() {
        connecte("etu1");
        when(evaluationService.initReponseEvaluation(42)).thenReturn(reponseAvecConvention());

        controller.createReponseEtudiant(42, false, new ReponseEtudiantFormDto());

        verify(mailerService, never()).sendValidationMail(any(), any(), any(Utilisateur.class),
                any(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void createReponseSansUtilisateurConnecteNEnvoiePasDeMail() {
        when(evaluationService.initReponseEvaluation(42)).thenReturn(reponseAvecConvention());

        controller.createReponseEtudiant(42, true, new ReponseEtudiantFormDto());

        verify(mailerService, never()).sendValidationMail(any(), any(), any(Utilisateur.class),
                any(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void updateReponseEtudiantEchoueSiAbsente() {
        when(reponseEvaluationJpaRepository.findByConvention(42)).thenReturn(null);

        assertThatThrownBy(() -> controller.updateReponseEtudiant(42, true, new ReponseEtudiantFormDto()))
                .isInstanceOf(AppException.class);
    }

    @Test
    void updateReponseEtudiantValideEnvoieLeMail() {
        connecte("etu1");
        ReponseEvaluation reponseEvaluation = reponseAvecConvention();
        when(reponseEvaluationJpaRepository.findByConvention(42)).thenReturn(reponseEvaluation);

        controller.updateReponseEtudiant(42, true, new ReponseEtudiantFormDto());

        assertThat(reponseEvaluation.getValidationEtudiant()).isTrue();
        verify(mailerService).sendValidationMail(any(), any(), any(Utilisateur.class),
                eq(TemplateMail.CODE_EVAL_ETU_REMPLIE), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void lesReponsesEnseignantSuiventLeMemeCycle() {
        connecte("ens1");
        ReponseEvaluation reponseEvaluation = reponseAvecConvention();
        when(evaluationService.initReponseEvaluation(42)).thenReturn(reponseEvaluation);
        when(reponseEvaluationJpaRepository.findByConvention(42)).thenReturn(reponseEvaluation);
        ReponseEnseignantFormDto form = new ReponseEnseignantFormDto();

        controller.createReponseEnseignant(42, true, form);
        assertThat(reponseEvaluation.getValidationEnseignant()).isTrue();
        verify(evaluationService).setReponseEvaluationEnseignantData(reponseEvaluation, form);

        controller.updateReponseEnseignant(42, false, form);
        assertThat(reponseEvaluation.getValidationEnseignant()).isFalse();

        assertThatThrownBy(() -> {
            when(reponseEvaluationJpaRepository.findByConvention(43)).thenReturn(null);
            controller.updateReponseEnseignant(43, true, form);
        }).isInstanceOf(AppException.class);
    }

    @Test
    void lesReponsesEntrepriseSuiventLeMemeCycle() {
        connecte("tut1");
        ReponseEvaluation reponseEvaluation = reponseAvecConvention();
        when(evaluationService.initReponseEvaluation(42)).thenReturn(reponseEvaluation);
        when(reponseEvaluationJpaRepository.findByConvention(42)).thenReturn(reponseEvaluation);
        ReponseEntrepriseFormDto form = new ReponseEntrepriseFormDto();

        controller.createReponseEntreprise(42, true, form);
        assertThat(reponseEvaluation.getValidationEntreprise()).isTrue();
        verify(evaluationService).setReponseEvaluationEntrepriseData(reponseEvaluation, form);

        controller.updateReponseEntreprise(42, true, form);

        assertThatThrownBy(() -> {
            when(reponseEvaluationJpaRepository.findByConvention(43)).thenReturn(null);
            controller.updateReponseEntreprise(43, true, form);
        }).isInstanceOf(AppException.class);
    }

    // ------------------------------------------------------------------
    // impression des fiches PDF
    // ------------------------------------------------------------------

    @Test
    void getFichePDFMarqueLImpressionSelonLeType() {
        ReponseEvaluation reponseEvaluation = reponseAvecConvention();
        when(reponseEvaluationJpaRepository.findByConvention(42)).thenReturn(reponseEvaluation);

        ResponseEntity<byte[]> pdfEtudiant = controller.getFichePDF(42, 0);
        assertThat(pdfEtudiant.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reponseEvaluation.getImpressionEtudiant()).isTrue();

        controller.getFichePDF(42, 1);
        assertThat(reponseEvaluation.getImpressionEnseignant()).isTrue();

        controller.getFichePDF(42, 2);
        assertThat(reponseEvaluation.getImpressionEntreprise()).isTrue();

        verify(impressionService, org.mockito.Mockito.times(3))
                .generateEvaluationPDF(eq(reponseEvaluation.getConvention()), any(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void getFichePDFEchoueSiReponseInconnue() {
        when(reponseEvaluationJpaRepository.findByConvention(99)).thenReturn(null);

        assertThatThrownBy(() -> controller.getFichePDF(99, 0))
                .isInstanceOf(AppException.class);
    }
}
