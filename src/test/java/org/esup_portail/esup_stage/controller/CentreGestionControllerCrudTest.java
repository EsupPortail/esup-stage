package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ConventionService;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CentreGestionControllerCrudTest {

    private CentreGestionController controller;
    private CentreGestionRepository centreGestionRepository;
    private CentreGestionJpaRepository centreGestionJpaRepository;
    private CritereGestionJpaRepository critereGestionJpaRepository;
    private ConventionJpaRepository conventionJpaRepository;
    private ContactJpaRepository contactJpaRepository;
    private PersonnelCentreGestionJpaRepository personnelCentreGestionJpaRepository;
    private ConsigneJpaRepository consigneJpaRepository;
    private AppConfigService appConfigService;
    private ConventionService conventionService;
    private ConfidentialiteJpaRepository confidentialiteJpaRepository;

    @BeforeEach
    void setUp() {
        controller = new CentreGestionController();
        centreGestionRepository = mock(CentreGestionRepository.class);
        centreGestionJpaRepository = mock(CentreGestionJpaRepository.class);
        critereGestionJpaRepository = mock(CritereGestionJpaRepository.class);
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        contactJpaRepository = mock(ContactJpaRepository.class);
        personnelCentreGestionJpaRepository = mock(PersonnelCentreGestionJpaRepository.class);
        consigneJpaRepository = mock(ConsigneJpaRepository.class);
        appConfigService = mock(AppConfigService.class);
        conventionService = mock(ConventionService.class);
        confidentialiteJpaRepository = mock(ConfidentialiteJpaRepository.class);

        controller.centreGestionRepository = centreGestionRepository;
        controller.centreGestionJpaRepository = centreGestionJpaRepository;
        controller.critereGestionJpaRepository = critereGestionJpaRepository;
        controller.conventionJpaRepository = conventionJpaRepository;
        controller.contactJpaRepository = contactJpaRepository;
        controller.personnelCentreGestionJpaRepository = personnelCentreGestionJpaRepository;
        controller.consigneJpaRepository = consigneJpaRepository;
        controller.appConfigService = appConfigService;
        controller.conventionService = conventionService;
        controller.confidentialiteJpaRepository = confidentialiteJpaRepository;
        controller.confidentialiteService = new org.esup_portail.esup_stage.service.ConfidentialiteService();

        when(appConfigService.getConfigGenerale()).thenReturn(new ConfigGeneraleDto());
        when(centreGestionJpaRepository.saveAndFlush(any(CentreGestion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(confidentialiteJpaRepository.findById(anyString())).thenReturn(java.util.Optional.of(new Confidentialite()));
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

    @Test
    void searchNAffucheQueLesCentresValides() {
        when(centreGestionRepository.count(anyString())).thenReturn(1L);
        CentreGestion centre = new CentreGestion();
        centre.setNomCentre("Centre Sciences");
        when(centreGestionRepository.findPaginated(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(new ArrayList<>(List.of(centre)));

        PaginatedResponse<?> reponse = controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse());

        assertThat(reponse.getTotal()).isEqualTo(1L);
        assertThat(reponse.getData()).hasSize(1);
        // le filtre validationCreation=true est injecté d'office
        verify(centreGestionRepository).count(contains("validationCreation"));
    }

    @Test
    void searchTrieLesCentresDeLUtilisateurEnPremier() {
        connecte("ges1");
        when(centreGestionRepository.count(anyString())).thenReturn(2L);
        CentreGestion autre = new CentreGestion();
        autre.setPersonnels(List.of());
        CentreGestion mien = new CentreGestion();
        PersonnelCentreGestion personnel = new PersonnelCentreGestion();
        personnel.setUidPersonnel("ges1");
        mien.setPersonnels(List.of(personnel));
        when(centreGestionRepository.findPaginated(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(new ArrayList<>(List.of(autre, mien)));

        PaginatedResponse<?> reponse = controller.search(1, 50, "personnels", "asc", "{}", new MockHttpServletResponse());

        assertThat(reponse.getData()).hasSize(2);
    }

    @Test
    void brouillonExistantOuNeufSelonLUtilisateur() {
        connecte("ges1");
        CentreGestion brouillon = new CentreGestion();
        when(centreGestionJpaRepository.findBrouillon("ges1")).thenReturn(brouillon);
        assertThat(controller.getBrouillonByLogin()).isSameAs(brouillon);

        when(centreGestionJpaRepository.findBrouillon("ges1")).thenReturn(null);
        assertThat(controller.getBrouillonByLogin()).isNotNull();
    }

    @Test
    void centreEtablissementIntrouvableEchoue() {
        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(null);
        assertThatThrownBy(() -> controller.getCentreEtablissement()).isInstanceOf(AppException.class);

        CentreGestion etablissement = new CentreGestion();
        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(etablissement);
        assertThat(controller.getCentreEtablissement()).isSameAs(etablissement);
    }

    @Test
    void getByIdRenvoieUnCentreVideSiInconnu() {
        when(centreGestionJpaRepository.findById(7)).thenReturn(null);
        assertThat(controller.getById(7)).isNotNull();

        CentreGestion centre = new CentreGestion();
        when(centreGestionJpaRepository.findById(8)).thenReturn(centre);
        assertThat(controller.getById(8)).isSameAs(centre);
    }

    @Test
    void createInitialiseSignatairesEtConsigne() {
        ConfigGeneraleDto config = new ConfigGeneraleDto();
        config.setCodeUniversite("UL");
        when(appConfigService.getConfigGenerale()).thenReturn(config);
        List<CentreGestionSignataire> signataires = List.of();
        when(conventionService.initSignataires(any())).thenReturn(signataires);

        CentreGestion centre = new CentreGestion();
        CentreGestion cree = controller.create(centre);

        assertThat(cree.getCodeUniversite()).isEqualTo("UL");
        assertThat(cree.getSignataires()).isSameAs(signataires);
        verify(consigneJpaRepository).saveAndFlush(any(Consigne.class));
    }

    @Test
    void updatePropageLaVerificationAdministrative() {
        CentreGestion centre = new CentreGestion();
        centre.setId(5);
        centre.setVerificationAdministrative(true);
        Consigne consigne = new Consigne();
        centre.setConsigne(consigne);
        when(centreGestionJpaRepository.findById(5)).thenReturn(null);

        CentreGestion resultat = controller.update(centre);

        verify(conventionJpaRepository).updateVerificationAdministrative(5);
        assertThat(resultat.getFicheEvaluation()).isNull();
        assertThat(consigne.getCentreGestion()).isSameAs(centre);
    }

    @Test
    void updateConserveLesCriteresExistantsSiAbsents() {
        CentreGestion demande = new CentreGestion();
        demande.setId(5);
        CentreGestion actuel = new CentreGestion();
        CritereGestion critere = new CritereGestion();
        actuel.setCriteres(new ArrayList<>(List.of(critere)));
        when(centreGestionJpaRepository.findById(5)).thenReturn(actuel);

        CentreGestion resultat = controller.update(demande);

        assertThat(resultat.getCriteres()).containsExactly(critere);
    }

    @Test
    void validationCreationBasculeLeFlag() {
        CentreGestion centre = new CentreGestion();
        centre.setConsigne(new Consigne());
        when(centreGestionJpaRepository.findById(5)).thenReturn(centre);

        CentreGestion resultat = controller.validationCreation(5);

        assertThat(resultat.isValidationCreation()).isTrue();
    }

    @Test
    void lesCompteursDelegentAuxRepositories() {
        when(conventionJpaRepository.countConventionWithCentreGestion(5)).thenReturn(3L);
        when(contactJpaRepository.countContactWithCentreGestion(5)).thenReturn(2L);
        CentreGestion centre = new CentreGestion();
        centre.setCriteres(new ArrayList<>(List.of(new CritereGestion())));
        when(centreGestionJpaRepository.findById(5)).thenReturn(centre);

        assertThat(controller.countConventionWithCentre(5)).isEqualTo(3L);
        assertThat(controller.countContactWithCentre(5)).isEqualTo(2L);
        assertThat(controller.countCritereWithCentre(5)).isEqualTo(1);
    }

    @Test
    void deleteNettoieLesRattachements() {
        controller.delete(5);

        verify(critereGestionJpaRepository).deleteCriteresByCentreId(5);
        verify(personnelCentreGestionJpaRepository).deletePersonnelsByCentreId(5);
        verify(centreGestionJpaRepository).deleteById(5);
    }

    // ------------------------------------------------------------------
    // composantes et étapes
    // ------------------------------------------------------------------

    private org.esup_portail.esup_stage.service.apogee.ApogeeService apogeeService() {
        org.esup_portail.esup_stage.service.apogee.ApogeeService apogeeService =
                mock(org.esup_portail.esup_stage.service.apogee.ApogeeService.class);
        controller.apogeeService = apogeeService;
        return apogeeService;
    }

    private org.esup_portail.esup_stage.service.apogee.model.Composante composante(String code, String libelle) {
        org.esup_portail.esup_stage.service.apogee.model.Composante composante =
                new org.esup_portail.esup_stage.service.apogee.model.Composante();
        composante.setCode(code);
        composante.setLibelle(libelle);
        return composante;
    }

    private CritereGestion critere(String code, String version, int idCentre) {
        CritereGestion critereGestion = new CritereGestion();
        CritereGestionId id = new CritereGestionId();
        id.setCode(code);
        id.setCodeVersionEtape(version);
        critereGestion.setId(id);
        critereGestion.setLibelle("Libellé " + code);
        CentreGestion centre = new CentreGestion();
        centre.setId(idCentre);
        critereGestion.setCentreGestion(centre);
        return critereGestion;
    }

    @Test
    void lesComposantesDejaPrisesParUnAutreCentreSontMasquees() {
        var apogee = apogeeService();
        when(apogee.getListComposante()).thenReturn(new java.util.ArrayList<>(List.of(
                composante("SCI", "Sciences"), composante("DRT", "Droit"))));
        when(critereGestionJpaRepository.findComposantes())
                .thenReturn(List.of(critere("DRT", "", 99))); // prise par le centre 99

        var composantes = controller.getComposantes(5);

        assertThat(composantes).extracting(org.esup_portail.esup_stage.service.apogee.model.Composante::getCode)
                .containsExactly("SCI");
    }

    @Test
    void lesComposantesDuCentreSontProjetees() {
        when(critereGestionJpaRepository.findByCentreId(5)).thenReturn(List.of(critere("SCI", "", 5)));

        var composantes = controller.getCentreComposante(5);

        assertThat(composantes).hasSize(1);
        assertThat(composantes.get(0).getCode()).isEqualTo("SCI");
    }

    @Test
    void setComposanteAjouteEtSupprime() {
        CentreGestion centre = new CentreGestion();
        centre.setId(5);
        when(centreGestionJpaRepository.findById(5)).thenReturn(centre);
        when(critereGestionJpaRepository.findByCentreId(5))
                .thenReturn(List.of(critere("ANCIENNE", "", 5)));
        when(conventionJpaRepository.countConventionRattacheUfr(5, "ANCIENNE")).thenReturn(0L);

        controller.setComposante(5, List.of(composante("NOUVELLE", "Nouvelle")));

        verify(critereGestionJpaRepository).delete(any(CritereGestion.class));
        verify(critereGestionJpaRepository).save(any(CritereGestion.class));
    }

    @Test
    void setComposanteRefuseLaSuppressionSiConventionRattachee() {
        CentreGestion centre = new CentreGestion();
        centre.setId(5);
        when(centreGestionJpaRepository.findById(5)).thenReturn(centre);
        when(critereGestionJpaRepository.findByCentreId(5))
                .thenReturn(List.of(critere("ANCIENNE", "", 5)));
        when(conventionJpaRepository.countConventionRattacheUfr(5, "ANCIENNE")).thenReturn(2L);

        assertThatThrownBy(() -> controller.setComposante(5, List.of()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("convention est déjà rattachée");
    }

    @Test
    void lesEtapesDuCentreSontProjetees() {
        when(critereGestionJpaRepository.findEtapesByCentreId(5))
                .thenReturn(List.of(critere("L3", "1", 5)));

        var etapes = controller.getCentreEtapes(5);

        assertThat(etapes).hasSize(1);
        assertThat(etapes.get(0).getCode()).isEqualTo("L3");
        assertThat(etapes.get(0).getCodeVrsEtp()).isEqualTo("1");
    }

    @Test
    void deleteEtapeRefuseSiConventionRattachee() {
        when(critereGestionJpaRepository.findEtapeById("L3", "1")).thenReturn(critere("L3", "1", 5));
        when(conventionJpaRepository.countConventionRattacheEtape(5, "L3", "1")).thenReturn(1L);

        assertThatThrownBy(() -> controller.deleteEtape("L3", "1"))
                .isInstanceOf(AppException.class);
    }

    @Test
    void deleteEtapeSupprimeQuandLibre() {
        when(critereGestionJpaRepository.findEtapeById("L3", "1")).thenReturn(critere("L3", "1", 5));
        when(conventionJpaRepository.countConventionRattacheEtape(5, "L3", "1")).thenReturn(0L);

        controller.deleteEtape("L3", "1");

        verify(critereGestionJpaRepository).delete(any(CritereGestion.class));
    }

    @Test
    void deleteEtapesCentreSupprimeToutOuRien() {
        when(centreGestionJpaRepository.findById(5)).thenReturn(new CentreGestion());
        when(critereGestionJpaRepository.findEtapesByCentreId(5))
                .thenReturn(List.of(critere("L3", "1", 5)));
        when(conventionJpaRepository.countConventionRattacheEtape(5, "L3", "1")).thenReturn(0L);

        controller.deleteEtapesCentre(5);
        verify(critereGestionJpaRepository).delete(any(CritereGestion.class));

        when(centreGestionJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.deleteEtapesCentre(99)).isInstanceOf(AppException.class);
    }
}
