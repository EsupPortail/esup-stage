package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.dto.ReponseEnseignantFormDto;
import org.esup_portail.esup_stage.dto.ReponseEntrepriseFormDto;
import org.esup_portail.esup_stage.dto.ReponseEtudiantFormDto;
import org.esup_portail.esup_stage.dto.ReponseSupplementaireFormDto;
import org.esup_portail.esup_stage.dto.SendMailEvaluationEnMasseResponseDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.ReponseEvaluationJpaRepository;
import org.esup_portail.esup_stage.repository.ReponseSupplementaireJpaRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReponseEvaluationControllerTest {

    private ReponseEvaluationController controller;
    private ReponseEvaluationJpaRepository reponseEvaluationJpaRepository;
    private ConventionJpaRepository conventionJpaRepository;
    private ReponseSupplementaireJpaRepository reponseSupplementaireJpaRepository;
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
        reponseSupplementaireJpaRepository = mock(ReponseSupplementaireJpaRepository.class);
        impressionService = mock(ImpressionService.class);
        mailerService = mock(MailerService.class);
        appConfigService = mock(AppConfigService.class);
        evaluationService = mock(EvaluationService.class);
        conventionService = mock(ConventionService.class);
        controller.reponseEvaluationJpaRepository = reponseEvaluationJpaRepository;
        controller.conventionJpaRepository = conventionJpaRepository;
        controller.reponseSupplementaireJpaRepository = reponseSupplementaireJpaRepository;
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

    // ------------------------------------------------------------------
    // envoi des mails d'évaluation
    // ------------------------------------------------------------------

    private Convention conventionComplete(int id) {
        Convention convention = new Convention();
        convention.setId(id);
        Etudiant etudiant = new Etudiant();
        etudiant.setMail("etu@univ.fr");
        convention.setEtudiant(etudiant);
        Enseignant enseignant = new Enseignant();
        enseignant.setMail("ens@univ.fr");
        convention.setEnseignant(enseignant);
        Contact contact = new Contact();
        contact.setMail("tuteur@acme.fr");
        convention.setContact(contact);
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setMail("centre@univ.fr");
        convention.setCentreGestion(centreGestion);
        return convention;
    }

    @Test
    void sendMailEvaluationEtudiantPuisRappel() {
        connecte("ges1");
        when(appConfigService.getConfigGenerale()).thenReturn(new ConfigGeneraleDto());
        Convention convention = conventionComplete(42);
        when(conventionJpaRepository.findById(42)).thenReturn(convention);

        controller.sendMailEvaluation(42, 0);
        verify(mailerService).sendAlerteValidation(eq("etu@univ.fr"), eq(convention), any(), any(Utilisateur.class),
                eq(TemplateMail.CODE_FICHE_EVAL_ETU));
        assertThat(convention.getEnvoiMailEtudiant()).isTrue();
        assertThat(convention.getDateEnvoiMailEtudiant()).isNotNull();

        // second envoi : mail de rappel
        controller.sendMailEvaluation(42, 0);
        verify(mailerService).sendAlerteValidation(eq("etu@univ.fr"), eq(convention), any(), any(Utilisateur.class),
                eq(TemplateMail.CODE_RAPPEL_FICHE_EVAL_ETU));

        when(conventionJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.sendMailEvaluation(99, 0)).isInstanceOf(AppException.class);
    }

    @Test
    void sendMailEvaluationUtiliseLeMailPersoSiConfigure() {
        connecte("ges1");
        ConfigGeneraleDto config = new ConfigGeneraleDto();
        config.setUtiliserMailPersoEtudiant(true);
        when(appConfigService.getConfigGenerale()).thenReturn(config);
        Convention convention = conventionComplete(42);
        convention.setCourrielPersoEtudiant("perso@gmail.com");
        when(conventionJpaRepository.findById(42)).thenReturn(convention);

        controller.sendMailEvaluation(42, 0);

        verify(mailerService).sendAlerteValidation(eq("perso@gmail.com"), eq(convention), any(), any(Utilisateur.class),
                eq(TemplateMail.CODE_FICHE_EVAL_ETU));
    }

    @Test
    void sendMailEvaluationEnseignantEnvoieLeRappel() {
        connecte("ges1");
        when(appConfigService.getConfigGenerale()).thenReturn(new ConfigGeneraleDto());
        Convention convention = conventionComplete(42);
        convention.setEnvoiMailTuteurPedago(true);
        when(conventionJpaRepository.findById(42)).thenReturn(convention);

        controller.sendMailEvaluation(42, 1);

        verify(mailerService).sendAlerteValidation(eq("ens@univ.fr"), eq(convention), any(), any(Utilisateur.class),
                eq(TemplateMail.CODE_RAPPEL_FICHE_EVAL_ENSEIGNANT));
        assertThat(convention.getEnvoiMailTuteurPedago()).isTrue();
    }

    @Test
    void sendMailEvaluationRespecteLeModeCentreGestionSeul() {
        connecte("ges1");
        when(appConfigService.getConfigGenerale()).thenReturn(new ConfigGeneraleDto());
        Convention convention = conventionComplete(42);
        convention.getCentreGestion().setOnlyMailCentreGestion(true);
        when(conventionJpaRepository.findById(42)).thenReturn(convention);

        // fiche étudiant : seul le centre est notifié, pas de drapeau posé
        controller.sendMailEvaluation(42, 0);
        verify(mailerService).sendAlerteValidation(eq("centre@univ.fr"), eq(convention), any(), any(Utilisateur.class),
                eq(TemplateMail.CODE_FICHE_EVAL_ETU));
        assertThat(convention.getEnvoiMailEtudiant()).isNull();

        // fiche enseignant : idem
        controller.sendMailEvaluation(42, 1);
        assertThat(convention.getEnvoiMailTuteurPedago()).isNull();

        // fiche tuteur : le tuteur reçoit toujours, le centre en plus
        controller.sendMailEvaluation(42, 2);
        verify(mailerService).sendAlerteValidation(eq("tuteur@acme.fr"), eq(convention), any(), any(Utilisateur.class),
                eq(TemplateMail.CODE_FICHE_EVAL_TUTEUR));
        assertThat(convention.getEnvoiMailTuteurPro()).isNull();
    }

    // ------------------------------------------------------------------
    // suppression + réponses supplémentaires
    // ------------------------------------------------------------------

    @Test
    void deleteSupprimeLaReponse() {
        ReponseEvaluation reponseEvaluation = new ReponseEvaluation();
        when(reponseEvaluationJpaRepository.findById(9)).thenReturn(reponseEvaluation);

        assertThat(controller.delete(9)).isTrue();
        verify(reponseEvaluationJpaRepository).delete(reponseEvaluation);

        when(reponseEvaluationJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.delete(99)).isInstanceOf(AppException.class);
    }

    @Test
    void lesReponsesSupplementairesSontGerees() {
        ReponseSupplementaire reponseSupplementaire = new ReponseSupplementaire();
        reponseSupplementaire.setReponseTxt("réponse existante");
        when(reponseSupplementaireJpaRepository.findByQuestionAndConvention(42, 7)).thenReturn(reponseSupplementaire);
        when(reponseSupplementaireJpaRepository.saveAndFlush(any(ReponseSupplementaire.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(controller.getReponseSupplementaire(42, 7)).isSameAs(reponseSupplementaire);

        when(evaluationService.initReponseSupplementaire(42, 7)).thenReturn(new ReponseSupplementaire());
        ReponseSupplementaireFormDto form = new ReponseSupplementaireFormDto();
        assertThat(controller.createReponseSupplementaire(42, 7, form)).isNotNull();

        assertThat(controller.updateReponseSupplementaire(42, 7, form)).isSameAs(reponseSupplementaire);
        verify(evaluationService).setReponseSupplementaireData(reponseSupplementaire, form);

        when(reponseSupplementaireJpaRepository.findByQuestionAndConvention(42, 8)).thenReturn(null);
        assertThatThrownBy(() -> controller.updateReponseSupplementaire(42, 8, form)).isInstanceOf(AppException.class);
    }

    // ------------------------------------------------------------------
    // envoi en masse
    // ------------------------------------------------------------------

    @Test
    void sendMailEnMasseValideLesParametres() {
        connecte("ges1");
        assertThatThrownBy(() -> controller.sendMailEvaluationEnMasse(0, List.of()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Aucune convention");
        assertThatThrownBy(() -> controller.sendMailEvaluationEnMasse(0, null)).isInstanceOf(AppException.class);
        assertThatThrownBy(() -> controller.sendMailEvaluationEnMasse(5, List.of(1)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Type fiche invalide");
    }

    @Test
    void sendMailEnMasseEtudiantChroniqueLesStatuts() {
        connecte("ges1");
        when(appConfigService.getConfigGenerale()).thenReturn(new ConfigGeneraleDto());
        Convention ok = conventionComplete(1);
        Convention sansMail = conventionComplete(2);
        sansMail.getEtudiant().setMail(null);
        when(conventionJpaRepository.findAllById(List.of(1, 2, 3))).thenReturn(List.of(ok, sansMail));

        SendMailEvaluationEnMasseResponseDto corps = controller.sendMailEvaluationEnMasse(0, List.of(1, 2, 3)).getBody();

        assertThat(corps.getResume().requested).isEqualTo(3);
        assertThat(corps.getResume().found).isEqualTo(2);
        assertThat(corps.getResume().sent).isEqualTo(1);
        assertThat(corps.getResume().failed).isEqualTo(2);
        assertThat(corps.getConventions()).extracting(r -> r.status).containsExactlyInAnyOrder("ERROR", "SENT", "ERROR");
        assertThat(ok.getEnvoiMailEtudiant()).isTrue();
        verify(mailerService).sendAlerteValidation(eq("etu@univ.fr"), eq(ok), any(), any(Utilisateur.class),
                eq(TemplateMail.CODE_FICHE_EVAL_ETU));
    }

    @Test
    void sendMailEnMasseGereRappelCentreEtExceptions() {
        connecte("ges1");
        when(appConfigService.getConfigGenerale()).thenReturn(new ConfigGeneraleDto());

        // fiche enseignant, rappel, uniquement vers le centre : pas de maj des drapeaux d'envoi
        Convention centreOnly = conventionComplete(1);
        centreOnly.getCentreGestion().setOnlyMailCentreGestion(true);
        centreOnly.setEnvoiMailTuteurPedago(true);
        when(conventionJpaRepository.findAllById(List.of(1))).thenReturn(List.of(centreOnly));
        SendMailEvaluationEnMasseResponseDto rep1 = controller.sendMailEvaluationEnMasse(1, List.of(1)).getBody();
        assertThat(rep1.getResume().sent).isEqualTo(1);
        verify(mailerService).sendAlerteValidation(eq("centre@univ.fr"), eq(centreOnly), any(), any(Utilisateur.class),
                eq(TemplateMail.CODE_RAPPEL_FICHE_EVAL_ENSEIGNANT));

        // fiche tuteur : l'envoi SMTP échoue → statut ERROR/exception
        Convention enErreur = conventionComplete(2);
        when(conventionJpaRepository.findAllById(List.of(2))).thenReturn(List.of(enErreur));
        org.mockito.Mockito.doThrow(new RuntimeException("smtp down")).when(mailerService)
                .sendAlerteValidation(eq("tuteur@acme.fr"), eq(enErreur), any(), any(Utilisateur.class), anyString());
        SendMailEvaluationEnMasseResponseDto rep2 = controller.sendMailEvaluationEnMasse(2, List.of(2)).getBody();
        assertThat(rep2.getResume().failed).isEqualTo(1);
        assertThat(rep2.getConventions().get(0).reason).isEqualTo("exception");
    }
}
