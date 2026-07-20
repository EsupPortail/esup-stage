package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ContactDetailDto;
import org.esup_portail.esup_stage.dto.ContactFormDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.ConfidentialiteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests des contrôleurs personnels de centre et contacts (endpoints simples).
 */
class PersonnelEtContactControllersTest {

    // ------------------------------------------------------------------
    // PersonnelCentreGestionController
    // ------------------------------------------------------------------

    private PersonnelCentreGestionController personnelController;
    private PersonnelCentreGestionRepository personnelCentreGestionRepository;
    private PersonnelCentreGestionJpaRepository personnelCentreGestionJpaRepository;

    // ------------------------------------------------------------------
    // ContactController
    // ------------------------------------------------------------------

    private ContactController contactController;
    private ContactJpaRepository contactJpaRepository;
    private ContactRepository contactRepository;
    private ServiceJpaRepository serviceJpaRepository;
    private CentreGestionJpaRepository centreGestionJpaRepository;
    private CiviliteJpaRepository civiliteJpaRepository;

    @BeforeEach
    void setUp() {
        personnelController = new PersonnelCentreGestionController();
        personnelCentreGestionRepository = mock(PersonnelCentreGestionRepository.class);
        personnelCentreGestionJpaRepository = mock(PersonnelCentreGestionJpaRepository.class);
        personnelController.personnelCentreGestionRepository = personnelCentreGestionRepository;
        personnelController.personnelCentreGestionJpaRepository = personnelCentreGestionJpaRepository;

        contactController = new ContactController();
        contactJpaRepository = mock(ContactJpaRepository.class);
        contactRepository = mock(ContactRepository.class);
        serviceJpaRepository = mock(ServiceJpaRepository.class);
        centreGestionJpaRepository = mock(CentreGestionJpaRepository.class);
        civiliteJpaRepository = mock(CiviliteJpaRepository.class);
        contactController.contactJpaRepository = contactJpaRepository;
        contactController.contactRepository = contactRepository;
        contactController.serviceJpaRepository = serviceJpaRepository;
        contactController.centreGestionJpaRepository = centreGestionJpaRepository;
        contactController.civiliteJpaRepository = civiliteJpaRepository;
        contactController.confidentialiteService = new ConfidentialiteService();
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
    void rechercheEtExportsDesPersonnels() {
        when(personnelCentreGestionRepository.count(anyString())).thenReturn(1L);
        when(personnelCentreGestionRepository.findPaginated(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(List.of(new PersonnelCentreGestion()));
        when(personnelCentreGestionRepository.exportExcel(any(), any(), any(), any())).thenReturn("xls".getBytes());
        when(personnelCentreGestionRepository.exportCsv(any(), any(), any(), any())).thenReturn(new StringBuilder("csv"));

        assertThat(personnelController.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse()).getTotal()).isEqualTo(1L);
        assertThat(personnelController.exportExcel("{}", "id", "asc", "{}", new MockHttpServletResponse()).getBody()).isNotEmpty();
        assertThat(personnelController.exportCsv("{}", "id", "asc", "{}", new MockHttpServletResponse()).getBody()).isEqualTo("csv");
    }

    @Test
    void updateCopieLesChampsDuPersonnel() {
        PersonnelCentreGestion existant = new PersonnelCentreGestion();
        when(personnelCentreGestionJpaRepository.findById(7)).thenReturn(existant);
        when(personnelCentreGestionJpaRepository.saveAndFlush(existant)).thenReturn(existant);

        PersonnelCentreGestion demande = new PersonnelCentreGestion();
        demande.setTel("0311111111");
        demande.setCampus("Campus A");
        demande.setBureau("B12");

        PersonnelCentreGestion resultat = personnelController.update(7, demande);

        assertThat(resultat.getTel()).isEqualTo("0311111111");
        assertThat(resultat.getCampus()).isEqualTo("Campus A");
        assertThat(resultat.getBureau()).isEqualTo("B12");
    }

    @Test
    void deleteSupprimeLePersonnel() {
        personnelController.delete(7);

        verify(personnelCentreGestionJpaRepository).deleteById(7);
        verify(personnelCentreGestionJpaRepository).flush();
    }

    @Test
    void unContactEstProjeteAvecSesChampsSensiblesPourUnAdmin() {
        connecte("adm1", Role.ADM);
        Contact contact = new Contact();
        contact.setId(9);
        contact.setNom("Martin");
        contact.setMail("martin@acme.fr");
        contact.setTel("0322222222");
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(3);
        contact.setCentreGestion(centreGestion);
        when(contactJpaRepository.findById(9)).thenReturn(contact);

        ContactDetailDto dto = contactController.getById(9);

        assertThat(dto.getNom()).isEqualTo("Martin");
        assertThat(dto.getMail()).isEqualTo("martin@acme.fr");
        assertThat(dto.getTel()).isEqualTo("0322222222");
    }

    @Test
    void lesChampsSensiblesSontMasquesPourUnEtudiant() {
        connecte("etu1", Role.ETU);
        Contact contact = new Contact();
        contact.setNom("Martin");
        contact.setMail("martin@acme.fr");
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(3);
        contact.setCentreGestion(centreGestion);
        when(contactJpaRepository.findById(9)).thenReturn(contact);

        ContactDetailDto dto = contactController.getById(9);

        assertThat(dto.getNom()).isEqualTo("Martin");
        assertThat(dto.getMail()).isNull();
    }

    @Test
    void unContactInconnuEchoue() {
        connecte("adm1", Role.ADM);
        when(contactJpaRepository.findById(99)).thenReturn(null);

        assertThatThrownBy(() -> contactController.getById(99)).isInstanceOf(AppException.class);
    }

    @Test
    void laRechercheDeContactsPourUnAdminNEstPasFiltree() {
        connecte("adm1", Role.ADM);
        Contact contact = new Contact();
        contact.setNom("Martin");
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(3);
        contact.setCentreGestion(centreGestion);
        when(contactRepository.count(anyString())).thenReturn(1L);
        when(contactRepository.findPaginated(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(List.of(contact));

        var reponse = contactController.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse());

        assertThat(reponse.getTotal()).isEqualTo(1L);
        assertThat(reponse.getData()).hasSize(1);
    }

    // ------------------------------------------------------------------
    // parcours gestionnaire
    // ------------------------------------------------------------------

    private Contact contactAvecCentre(int idCentre) {
        Contact contact = new Contact();
        contact.setNom("Martin");
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(idCentre);
        contact.setCentreGestion(centreGestion);
        return contact;
    }

    private CentreGestion centreGestionnaire(String uid, int id) {
        CentreGestion centre = new CentreGestion();
        centre.setId(id);
        when(centreGestionJpaRepository.findAllByGestionnaireUid(uid)).thenReturn(List.of(centre));
        return centre;
    }

    @Test
    void laRechercheDUnGestionnaireEstLimiteeASesCentres() {
        connecte("ges1", Role.GES);
        centreGestionnaire("ges1", 3);
        when(contactRepository.countVisibleForCentres(List.of(3), "{}")).thenReturn(1L);
        when(contactRepository.findPaginatedVisibleForCentres(List.of(3), 1, 50, "id", "asc", "{}"))
                .thenReturn(List.of(contactAvecCentre(3)));

        var reponse = contactController.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse());
        assertThat(reponse.getTotal()).isEqualTo(1L);
        assertThat(reponse.getData()).hasSize(1);

        // gestionnaire sans centre rattaché : accès refusé
        when(centreGestionJpaRepository.findAllByGestionnaireUid("ges1")).thenReturn(List.of());
        assertThatThrownBy(() -> contactController.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse()))
                .isInstanceOf(AppException.class);
    }

    @Test
    void getByIdPourUnGestionnairePasseParLaVisibilite() {
        connecte("ges1", Role.GES);
        centreGestionnaire("ges1", 3);
        Contact contact = contactAvecCentre(3);
        when(contactJpaRepository.findVisibleByIdForCentres(9, List.of(3))).thenReturn(contact);

        assertThat(contactController.getById(9).getNom()).isEqualTo("Martin");

        when(contactJpaRepository.findVisibleByIdForCentres(10, List.of(3))).thenReturn(null);
        assertThatThrownBy(() -> contactController.getById(10)).isInstanceOf(AppException.class);
    }

    @Test
    void getByServiceRetourneToutPourUnAdmin() {
        connecte("adm1", Role.ADM);
        when(contactJpaRepository.findByService(7)).thenReturn(List.of(contactAvecCentre(3)));

        assertThat(contactController.getByService(7, -1)).hasSize(1);

        when(contactJpaRepository.findByService(8)).thenReturn(null);
        assertThatThrownBy(() -> contactController.getByService(8, -1)).isInstanceOf(AppException.class);
    }

    @Test
    void getByServiceFiltreLesContactsInvisiblesDuGestionnaire() {
        connecte("ges1", Role.GES);
        CentreGestion centreDemandeur = centreGestionnaire("ges1", 3);
        ConfidentialiteService confidentialiteService = mock(ConfidentialiteService.class);
        contactController.confidentialiteService = confidentialiteService;

        Contact visible = contactAvecCentre(3);
        Contact invisible = contactAvecCentre(8);
        when(contactJpaRepository.findByService(7)).thenReturn(List.of(visible, invisible));
        when(confidentialiteService.canViewContact(centreDemandeur, visible)).thenReturn(true);
        when(confidentialiteService.canViewContact(centreDemandeur, invisible)).thenReturn(false);

        assertThat(contactController.getByService(7, -1)).hasSize(1);
        assertThat(contactController.getByServiceWithDetail(7, null)).hasSize(1);

        // gestionnaire sans centre : refus
        when(centreGestionJpaRepository.findAllByGestionnaireUid("ges1")).thenReturn(List.of());
        assertThatThrownBy(() -> contactController.getByService(7, -1)).isInstanceOf(AppException.class);
    }

    @Test
    void getByServicePourUneConventionFiltreParCentreCompatible() {
        connecte("ges1", Role.GES);
        CentreGestion centreDemandeur = centreGestionnaire("ges1", 3);
        ConfidentialiteService confidentialiteService = mock(ConfidentialiteService.class);
        contactController.confidentialiteService = confidentialiteService;

        Contact duCentreConvention = contactAvecCentre(5);
        Contact rattacheAuDemandeur = contactAvecCentre(3);
        Contact etranger = contactAvecCentre(8);
        when(contactJpaRepository.findByService(7)).thenReturn(List.of(duCentreConvention, rattacheAuDemandeur, etranger));
        when(confidentialiteService.canViewContact(any(CentreGestion.class), any(Contact.class))).thenReturn(true);

        CentreGestion centreConvention = new CentreGestion();
        centreConvention.setId(5);
        when(centreGestionJpaRepository.findById(5)).thenReturn(centreConvention);

        assertThat(contactController.getByService(7, 5)).hasSize(2); // centre convention + centre du demandeur

        // le contact étranger passe si son centre est sans confidentialité
        when(confidentialiteService.isNoConfidentiality(etranger.getCentreGestion())).thenReturn(true);
        assertThat(contactController.getByService(7, 5)).hasSize(3);

        // centre de convention inconnu
        when(centreGestionJpaRepository.findById(6)).thenReturn(null);
        assertThatThrownBy(() -> contactController.getByService(7, 6)).isInstanceOf(AppException.class);
    }

    // ------------------------------------------------------------------
    // create / update / delete
    // ------------------------------------------------------------------

    private ContactFormDto formulaireContact(Integer idCentreGestion) {
        ContactFormDto dto = new ContactFormDto();
        dto.setNom("Durand");
        dto.setPrenom("Paul");
        dto.setFonction("RH");
        dto.setTel("0102030405");
        dto.setMail("p.durand@acme.fr");
        dto.setIdService(7);
        dto.setIdCivilite(1);
        dto.setIdCentreGestion(idCentreGestion);
        return dto;
    }

    @Test
    void createResoutLeCentreSelonLeRole() {
        when(civiliteJpaRepository.findById(1)).thenReturn(new Civilite());
        when(contactJpaRepository.saveAndFlush(any(Contact.class))).thenAnswer(inv -> inv.getArgument(0));

        // service inconnu
        connecte("adm1", Role.ADM);
        when(serviceJpaRepository.findById(7)).thenReturn(null);
        assertThatThrownBy(() -> contactController.create(formulaireContact(null)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Service");

        // admin sans centre demandé : centre du service
        Service service = new Service();
        CentreGestion centreService = new CentreGestion();
        centreService.setId(4);
        service.setCentreGestion(centreService);
        when(serviceJpaRepository.findById(7)).thenReturn(service);
        Contact cree = contactController.create(formulaireContact(null));
        assertThat(cree.getCentreGestion()).isSameAs(centreService);
        assertThat(cree.getNom()).isEqualTo("Durand");

        // admin, service sans centre : centre établissement
        Service sansCentre = new Service();
        when(serviceJpaRepository.findById(7)).thenReturn(sansCentre);
        CentreGestion etablissement = new CentreGestion();
        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(etablissement);
        assertThat(contactController.create(formulaireContact(null)).getCentreGestion()).isSameAs(etablissement);

        // admin avec centre demandé explicite
        CentreGestion demande = new CentreGestion();
        when(centreGestionJpaRepository.findById(5)).thenReturn(demande);
        assertThat(contactController.create(formulaireContact(5)).getCentreGestion()).isSameAs(demande);

        // centre demandé introuvable
        when(centreGestionJpaRepository.findById(6)).thenReturn(null);
        assertThatThrownBy(() -> contactController.create(formulaireContact(6)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("CentreGestion");
    }

    @Test
    void createPourUnGestionnaireImposeSesCentres() {
        when(civiliteJpaRepository.findById(1)).thenReturn(new Civilite());
        when(contactJpaRepository.saveAndFlush(any(Contact.class))).thenAnswer(inv -> inv.getArgument(0));
        when(serviceJpaRepository.findById(7)).thenReturn(new Service());
        connecte("ges1", Role.GES);

        // un seul centre rattaché : choisi automatiquement
        CentreGestion centre = centreGestionnaire("ges1", 3);
        assertThat(contactController.create(formulaireContact(null)).getCentreGestion()).isSameAs(centre);

        // choix explicite d'un centre rattaché
        assertThat(contactController.create(formulaireContact(3)).getCentreGestion()).isSameAs(centre);

        // centre non rattaché au gestionnaire : refus
        assertThatThrownBy(() -> contactController.create(formulaireContact(99)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("rattache");

        // plusieurs centres sans choix explicite : refus
        CentreGestion autre = new CentreGestion();
        autre.setId(4);
        when(centreGestionJpaRepository.findAllByGestionnaireUid("ges1")).thenReturn(List.of(centre, autre));
        assertThatThrownBy(() -> contactController.create(formulaireContact(null)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("doit etre renseigne");

        // aucun centre rattaché : refus
        when(centreGestionJpaRepository.findAllByGestionnaireUid("ges1")).thenReturn(List.of());
        assertThatThrownBy(() -> contactController.create(formulaireContact(null)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Impossible de determiner");
    }

    @Test
    void updateCopieLesChampsDuContact() {
        when(civiliteJpaRepository.findById(1)).thenReturn(new Civilite());
        when(contactJpaRepository.saveAndFlush(any(Contact.class))).thenAnswer(inv -> inv.getArgument(0));
        Contact contact = contactAvecCentre(3);
        when(contactJpaRepository.findById(9)).thenReturn(contact);

        Contact modifie = contactController.update(9, formulaireContact(null));

        assertThat(modifie.getNom()).isEqualTo("Durand");
        assertThat(modifie.getMail()).isEqualTo("p.durand@acme.fr");

        // contact inconnu
        when(contactJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> contactController.update(99, formulaireContact(null))).isInstanceOf(AppException.class);
    }

    @Test
    void deleteSupprimeLeContact() {
        Contact contact = contactAvecCentre(3);
        when(contactJpaRepository.findById(9)).thenReturn(contact);

        assertThat(contactController.delete(9)).isTrue();
        verify(contactJpaRepository).delete(contact);

        when(contactJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> contactController.delete(99)).isInstanceOf(AppException.class);
    }
}
