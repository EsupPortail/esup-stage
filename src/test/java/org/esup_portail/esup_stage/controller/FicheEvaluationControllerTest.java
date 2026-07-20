package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.FicheEnseignantDto;
import org.esup_portail.esup_stage.dto.FicheEntrepriseDto;
import org.esup_portail.esup_stage.dto.FicheEtudiantDto;
import org.esup_portail.esup_stage.dto.QuestionSupplementaireDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.EtudiantSecurityService;
import org.esup_portail.esup_stage.service.evaluation.EvaluationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FicheEvaluationControllerTest {

    private FicheEvaluationController controller;
    private FicheEvaluationRepository ficheEvaluationRepository;
    private FicheEvaluationJpaRepository ficheEvaluationJpaRepository;
    private QuestionSupplementaireJpaRepository questionSupplementaireJpaRepository;
    private CentreGestionJpaRepository centreGestionJpaRepository;
    private EvaluationService evaluationService;
    private ConventionJpaRepository conventionJpaRepository;
    private EtudiantSecurityService etudiantSecurityService;
    private PersonnelCentreGestionJpaRepository personnelCentreGestionJpaRepository;

    @BeforeEach
    void setUp() {
        controller = new FicheEvaluationController();
        ficheEvaluationRepository = mock(FicheEvaluationRepository.class);
        ficheEvaluationJpaRepository = mock(FicheEvaluationJpaRepository.class);
        questionSupplementaireJpaRepository = mock(QuestionSupplementaireJpaRepository.class);
        centreGestionJpaRepository = mock(CentreGestionJpaRepository.class);
        evaluationService = mock(EvaluationService.class);
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        etudiantSecurityService = mock(EtudiantSecurityService.class);
        personnelCentreGestionJpaRepository = mock(PersonnelCentreGestionJpaRepository.class);
        controller.ficheEvaluationRepository = ficheEvaluationRepository;
        controller.ficheEvaluationJpaRepository = ficheEvaluationJpaRepository;
        controller.questionSupplementaireJpaRepository = questionSupplementaireJpaRepository;
        controller.centreGestionJpaRepository = centreGestionJpaRepository;
        ReflectionTestUtils.setField(controller, "evaluationService", evaluationService);
        controller.conventionJpaRepository = conventionJpaRepository;
        controller.etudiantSecurityService = etudiantSecurityService;
        controller.personnelCentreGestionJpaRepository = personnelCentreGestionJpaRepository;

        when(ficheEvaluationJpaRepository.saveAndFlush(any(FicheEvaluation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(questionSupplementaireJpaRepository.saveAndFlush(any(QuestionSupplementaire.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void connecte(String uid, String... roleCodes) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUid(uid);
        utilisateur.setLogin(uid);
        utilisateur.setRoles(java.util.Arrays.stream(roleCodes).map(code -> {
            Role role = new Role();
            role.setCode(code);
            return role;
        }).toList());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CasUserDetailsImpl(utilisateur, List.of()), null));
    }

    @Test
    void searchEtGetByIdDeleguentAuxRepositories() {
        when(ficheEvaluationRepository.count("{}")).thenReturn(1L);
        when(ficheEvaluationRepository.findPaginated(1, 50, "id", "asc", "{}")).thenReturn(List.of(new FicheEvaluation()));
        assertThat(controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse()).getTotal()).isEqualTo(1L);

        FicheEvaluation fiche = new FicheEvaluation();
        when(ficheEvaluationJpaRepository.findById(7)).thenReturn(fiche);
        assertThat(controller.getById(7)).isSameAs(fiche);
        when(ficheEvaluationJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.getById(99)).isInstanceOf(AppException.class);
    }

    private CentreGestion centre(int id) {
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(id);
        return centreGestion;
    }

    @Test
    void getByCentreGestionCreeLaFicheSiAbsente() {
        connecte("adm1", Role.ADM);
        CentreGestion centreGestion = centre(3);
        when(centreGestionJpaRepository.findById(3)).thenReturn(centreGestion);

        FicheEvaluation existante = new FicheEvaluation();
        when(ficheEvaluationJpaRepository.findByCentreGestion(3)).thenReturn(existante);
        assertThat(controller.getByCentreGestion(3)).isSameAs(existante);

        when(ficheEvaluationJpaRepository.findByCentreGestion(3)).thenReturn(null);
        FicheEvaluation creee = controller.getByCentreGestion(3);
        assertThat(creee.getCentreGestion()).isSameAs(centreGestion);
        verify(ficheEvaluationJpaRepository).saveAndFlush(creee);

        when(centreGestionJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.getByCentreGestion(99)).isInstanceOf(AppException.class);
    }

    @Test
    void lAccesAuCentreDependDuRole() {
        CentreGestion centreGestion = centre(3);
        when(centreGestionJpaRepository.findById(3)).thenReturn(centreGestion);
        when(ficheEvaluationJpaRepository.findByCentreGestion(3)).thenReturn(new FicheEvaluation());

        // gestionnaire rattaché au centre
        connecte("ges1", Role.GES);
        when(personnelCentreGestionJpaRepository.countByCentreGestionAndUidPersonnel(eq(3), anyList())).thenReturn(1L);
        assertThat(controller.getByCentreGestion(3)).isNotNull();

        // gestionnaire non rattaché
        when(personnelCentreGestionJpaRepository.countByCentreGestionAndUidPersonnel(eq(3), anyList())).thenReturn(0L);
        assertThatThrownBy(() -> controller.getByCentreGestion(3)).isInstanceOf(AppException.class);

        // étudiant du centre
        connecte("etu1", Role.ETU);
        when(etudiantSecurityService.isEtudiantInCentreGestion(any(Utilisateur.class), eq(3))).thenReturn(true);
        assertThat(controller.getByCentreGestion(3)).isNotNull();

        // enseignant avec convention dans le centre
        connecte("ens1", Role.ENS);
        when(conventionJpaRepository.countConventionByEnseignantAndCentreGestion(anyList(), eq(3))).thenReturn(2L);
        assertThat(controller.getByCentreGestion(3)).isNotNull();

        // enseignant sans convention
        when(conventionJpaRepository.countConventionByEnseignantAndCentreGestion(anyList(), eq(3))).thenReturn(0L);
        assertThatThrownBy(() -> controller.getByCentreGestion(3)).isInstanceOf(AppException.class);

        // personne de connecté
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> controller.getByCentreGestion(3))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void lesTroisFichesSontSauveesEtValidees() {
        FicheEvaluation fiche = new FicheEvaluation();
        when(ficheEvaluationJpaRepository.findById(7)).thenReturn(fiche);

        assertThat(controller.saveAndValidateFicheEtudiant(7, new FicheEtudiantDto()).getValidationEtudiant()).isTrue();
        verify(evaluationService).setFicheEtudiantData(eq(fiche), any(FicheEtudiantDto.class));

        assertThat(controller.saveAndValidateFicheEnseignant(7, new FicheEnseignantDto()).getValidationEnseignant()).isTrue();
        verify(evaluationService).setFicheEnseignantData(eq(fiche), any(FicheEnseignantDto.class));

        assertThat(controller.saveAndValidateFicheEntreprise(7, new FicheEntrepriseDto()).getValidationEntreprise()).isTrue();
        verify(evaluationService).setFicheEntrepriseData(eq(fiche), any(FicheEntrepriseDto.class));

        when(ficheEvaluationJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.saveAndValidateFicheEtudiant(99, new FicheEtudiantDto())).isInstanceOf(AppException.class);
        assertThatThrownBy(() -> controller.saveAndValidateFicheEnseignant(99, new FicheEnseignantDto())).isInstanceOf(AppException.class);
        assertThatThrownBy(() -> controller.saveAndValidateFicheEntreprise(99, new FicheEntrepriseDto())).isInstanceOf(AppException.class);
    }

    @Test
    void lesQuestionsSupplementairesSontGerees() {
        when(questionSupplementaireJpaRepository.findByFicheEvaluation(7)).thenReturn(List.of(new QuestionSupplementaire()));
        assertThat(controller.getQuestionsSupplementaires(7)).hasSize(1);

        FicheEvaluation fiche = new FicheEvaluation();
        when(ficheEvaluationJpaRepository.findById(7)).thenReturn(fiche);
        QuestionSupplementaire ajoutee = controller.addQuestionSupplementaire(7, new QuestionSupplementaireDto());
        assertThat(ajoutee.getFicheEvaluation()).isSameAs(fiche);
        verify(evaluationService).setQuestionSupplementaireData(eq(ajoutee), any(QuestionSupplementaireDto.class));

        QuestionSupplementaire question = new QuestionSupplementaire();
        when(questionSupplementaireJpaRepository.findById(4)).thenReturn(question);
        assertThat(controller.editQuestionSupplementaire(4, new QuestionSupplementaireDto())).isSameAs(question);
        assertThat(controller.deleteQuestionSupplementaire(4)).isTrue();
        verify(questionSupplementaireJpaRepository).delete(question);

        when(ficheEvaluationJpaRepository.findById(99)).thenReturn(null);
        when(questionSupplementaireJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.addQuestionSupplementaire(99, new QuestionSupplementaireDto())).isInstanceOf(AppException.class);
        assertThatThrownBy(() -> controller.editQuestionSupplementaire(99, new QuestionSupplementaireDto())).isInstanceOf(AppException.class);
        assertThatThrownBy(() -> controller.deleteQuestionSupplementaire(99)).isInstanceOf(AppException.class);
    }

    @Test
    void deleteSupprimeLaFiche() {
        FicheEvaluation fiche = new FicheEvaluation();
        when(ficheEvaluationJpaRepository.findById(7)).thenReturn(fiche);
        assertThat(controller.delete(7)).isTrue();
        verify(ficheEvaluationJpaRepository).delete(fiche);

        when(ficheEvaluationJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.delete(99)).isInstanceOf(AppException.class);
    }
}
