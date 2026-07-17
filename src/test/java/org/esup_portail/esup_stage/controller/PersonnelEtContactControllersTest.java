package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ContactDetailDto;
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
        contactController.contactJpaRepository = contactJpaRepository;
        contactController.contactRepository = contactRepository;
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
}
