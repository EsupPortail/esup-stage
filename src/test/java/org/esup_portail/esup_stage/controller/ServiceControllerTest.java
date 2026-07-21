package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ServiceDto;
import org.esup_portail.esup_stage.dto.ServiceFormDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.ConfidentialiteAccessService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceControllerTest {

    private ServiceController controller;
    private ServiceRepository serviceRepository;
    private ServiceJpaRepository serviceJpaRepository;
    private ContactJpaRepository contactJpaRepository;
    private CentreGestionJpaRepository centreGestionJpaRepository;
    private StructureJpaRepository structureJpaRepository;
    private PaysJpaRepository paysJpaRepository;
    private ConfidentialiteAccessService confidentialiteAccessService;

    @BeforeEach
    void setUp() {
        controller = new ServiceController();
        serviceRepository = mock(ServiceRepository.class);
        serviceJpaRepository = mock(ServiceJpaRepository.class);
        contactJpaRepository = mock(ContactJpaRepository.class);
        centreGestionJpaRepository = mock(CentreGestionJpaRepository.class);
        structureJpaRepository = mock(StructureJpaRepository.class);
        paysJpaRepository = mock(PaysJpaRepository.class);
        confidentialiteAccessService = mock(ConfidentialiteAccessService.class);
        controller.serviceRepository = serviceRepository;
        controller.serviceJpaRepository = serviceJpaRepository;
        controller.contactJpaRepository = contactJpaRepository;
        controller.centreGestionJpaRepository = centreGestionJpaRepository;
        controller.structureJpaRepository = structureJpaRepository;
        controller.paysJpaRepository = paysJpaRepository;
        controller.confidentialiteAccessService = confidentialiteAccessService;

        when(serviceJpaRepository.saveAndFlush(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));
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

    private CentreGestion centre(int id) {
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(id);
        return centreGestion;
    }

    private Service service(String nom, CentreGestion centreGestion) {
        Service service = new Service();
        service.setNom(nom);
        service.setCentreGestion(centreGestion);
        return service;
    }

    @Test
    void laRechercheDUnAdminNEstPasFiltree() {
        connecte("adm1", Role.ADM);
        when(serviceRepository.count("{}")).thenReturn(1L);
        when(serviceRepository.findPaginated(1, 50, "id", "asc", "{}"))
                .thenReturn(List.of(service("Service RH", centre(3))));

        var reponse = controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse());

        assertThat(reponse.getTotal()).isEqualTo(1L);
        assertThat(reponse.getData()).extracting(ServiceDto::getNom).containsExactly("Service RH");
        assertThat(reponse.getData().get(0).getIdCentreGestion()).isEqualTo(3);
    }

    @Test
    void laRechercheDUnGestionnaireEstRestreinteASesCentres() {
        connecte("ges1", Role.GES);
        when(confidentialiteAccessService.isGestionnaire(any())).thenReturn(true);
        when(confidentialiteAccessService.getCentresDemandeur(any())).thenReturn(List.of(centre(3)));
        when(serviceRepository.countVisibleForCentres(List.of(3), "{}")).thenReturn(1L);
        when(serviceRepository.findPaginatedVisibleForCentres(List.of(3), 1, 50, "id", "asc", "{}"))
                .thenReturn(List.of(service("Service RH", centre(3))));

        var reponse = controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse());
        assertThat(reponse.getTotal()).isEqualTo(1L);

        when(confidentialiteAccessService.getCentresDemandeur(any())).thenReturn(List.of());
        assertThatThrownBy(() -> controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse()))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void getByIdControleLaConfidentialitePourLesGestionnaires() {
        Service service = service("Service RH", centre(3));
        when(serviceJpaRepository.findById(7)).thenReturn(service);

        connecte("adm1", Role.ADM);
        assertThat(controller.getById(7).getNom()).isEqualTo("Service RH");

        connecte("ges1", Role.GES);
        when(confidentialiteAccessService.isGestionnaire(any())).thenReturn(true);
        when(confidentialiteAccessService.getCentresDemandeur(any())).thenReturn(List.of(centre(3)));
        when(confidentialiteAccessService.canViewService(anyList(), any(Service.class))).thenReturn(true);
        assertThat(controller.getById(7)).isNotNull();

        when(confidentialiteAccessService.canViewService(anyList(), any(Service.class))).thenReturn(false);
        assertThatThrownBy(() -> controller.getById(7))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        when(serviceJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.getById(99)).isInstanceOf(AppException.class);
    }

    @Test
    void getByStructureFiltreSelonLeCentreDeLaConvention() {
        Service visible = service("Visible", centre(3));
        Service autre = service("Autre", centre(8));
        when(serviceJpaRepository.findByStructure(5)).thenReturn(List.of(visible, autre));

        connecte("adm1", Role.ADM);
        assertThat(controller.getByStructure(5, -1)).hasSize(2);

        connecte("ges1", Role.GES);
        when(confidentialiteAccessService.isGestionnaire(any())).thenReturn(true);
        when(confidentialiteAccessService.getCentresDemandeur(any())).thenReturn(List.of(centre(3)));
        when(confidentialiteAccessService.canViewService(anyList(), any(Service.class))).thenReturn(true);
        when(confidentialiteAccessService.canUseServiceForConvention(any(Service.class), any(CentreGestion.class), anyList())).thenReturn(true);
        when(centreGestionJpaRepository.findById(3)).thenReturn(centre(3));

        List<ServiceDto> filtres = controller.getByStructure(5, 3);
        // "Visible" garde le centre 3 (= centre convention), "Autre" est rattaché au centre 8 mais le
        // gestionnaire est attaché au centre 3 uniquement : il passe quand même si aucune confidentialité
        assertThat(filtres).extracting(ServiceDto::getNom).contains("Visible");

        when(centreGestionJpaRepository.findById(44)).thenReturn(null);
        assertThatThrownBy(() -> controller.getByStructure(5, 44)).isInstanceOf(AppException.class);

        when(confidentialiteAccessService.getCentresDemandeur(any())).thenReturn(List.of());
        assertThatThrownBy(() -> controller.getByStructure(5, -1)).isInstanceOf(AppException.class);
    }

    private ServiceFormDto formulaire(int idPays, int idStructure) {
        ServiceFormDto dto = new ServiceFormDto();
        dto.setNom("Service RH");
        dto.setVoie("1 rue Haute");
        dto.setCodePostal("54000");
        dto.setBatimentResidence("Bât. B");
        dto.setCommune("Nancy");
        dto.setTelephone("0383000000");
        dto.setIdPays(idPays);
        dto.setIdStructure(idStructure);
        return dto;
    }

    @Test
    void createRattacheLeServiceAuBonCentre() {
        connecte("adm1", Role.ADM);
        when(paysJpaRepository.findById(1)).thenReturn(new Pays());

        Structure structure = new Structure();
        CentreGestion proprietaire = centre(3);
        structure.setCentreGestionProprietaire(proprietaire);
        when(structureJpaRepository.findById(5)).thenReturn(structure);

        Service cree = controller.create(formulaire(1, 5));
        assertThat(cree.getNom()).isEqualTo("Service RH");
        assertThat(cree.getCentreGestion()).isSameAs(proprietaire);
        assertThat(cree.getStructure()).isSameAs(structure);

        // sans centre propriétaire : centre établissement
        Structure sansProprietaire = new Structure();
        when(structureJpaRepository.findById(6)).thenReturn(sansProprietaire);
        CentreGestion etablissement = centre(9);
        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(etablissement);
        assertThat(controller.create(formulaire(1, 6)).getCentreGestion()).isSameAs(etablissement);
    }

    @Test
    void createPourUnGestionnaireUtiliseLeCentreDemande() {
        connecte("ges1", Role.GES);
        when(confidentialiteAccessService.isGestionnaire(any())).thenReturn(true);
        when(paysJpaRepository.findById(1)).thenReturn(new Pays());
        when(structureJpaRepository.findById(5)).thenReturn(new Structure());
        CentreGestion demande = centre(4);
        when(centreGestionJpaRepository.findById(4)).thenReturn(demande);

        ServiceFormDto dto = formulaire(1, 5);
        dto.setIdCentreGestion(4);
        assertThat(controller.create(dto).getCentreGestion()).isSameAs(demande);

        // centre introuvable
        when(centreGestionJpaRepository.findById(4)).thenReturn(null);
        assertThatThrownBy(() -> controller.create(dto)).isInstanceOf(AppException.class);
    }

    @Test
    void createControleSesEntrees() {
        connecte("adm1", Role.ADM);
        when(paysJpaRepository.findById(1)).thenReturn(new Pays());
        when(structureJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.create(formulaire(1, 99))).isInstanceOf(AppException.class);

        when(paysJpaRepository.findById(88)).thenReturn(null);
        assertThatThrownBy(() -> controller.create(formulaire(88, 5)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Pays");
    }

    @Test
    void updateModifieLeServiceExistant() {
        connecte("adm1", Role.ADM);
        when(paysJpaRepository.findById(1)).thenReturn(new Pays());
        Service service = service("Ancien", centre(3));
        when(serviceJpaRepository.findById(7)).thenReturn(service);

        Service maj = controller.update(7, formulaire(1, 5));
        assertThat(maj.getNom()).isEqualTo("Service RH");
        assertThat(maj.getCommune()).isEqualTo("Nancy");

        when(serviceJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.update(99, formulaire(1, 5))).isInstanceOf(AppException.class);
    }

    @Test
    void deleteRefuseSiDesContactsExistent() {
        Service service = service("Service RH", centre(3));
        service.setId(7);
        when(serviceJpaRepository.findById(7)).thenReturn(service);
        when(contactJpaRepository.countContactWithService(7)).thenReturn(2L);

        assertThatThrownBy(() -> controller.delete(7))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("contacts existent");

        when(contactJpaRepository.countContactWithService(7)).thenReturn(0L);
        assertThat(controller.delete(7)).isTrue();
        verify(serviceJpaRepository).delete(service);

        when(serviceJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.delete(99)).isInstanceOf(AppException.class);
    }
}
