package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.AppFonction;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.RoleAppFonction;
import org.esup_portail.esup_stage.repository.AppFonctionJpaRepository;
import org.esup_portail.esup_stage.repository.RoleJpaRepository;
import org.esup_portail.esup_stage.repository.RoleRepository;
import org.esup_portail.esup_stage.repository.UtilisateurJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoleControllerTest {

    private RoleController controller;
    private RoleRepository roleRepository;
    private RoleJpaRepository roleJpaRepository;
    private AppFonctionJpaRepository appFonctionJpaRepository;
    private UtilisateurJpaRepository utilisateurJpaRepository;

    @BeforeEach
    void setUp() {
        controller = new RoleController();
        roleRepository = mock(RoleRepository.class);
        roleJpaRepository = mock(RoleJpaRepository.class);
        appFonctionJpaRepository = mock(AppFonctionJpaRepository.class);
        utilisateurJpaRepository = mock(UtilisateurJpaRepository.class);
        controller.roleRepository = roleRepository;
        controller.roleJpaRepository = roleJpaRepository;
        controller.appFonctionJpaRepository = appFonctionJpaRepository;
        controller.utilisateurJpaRepository = utilisateurJpaRepository;

        when(roleJpaRepository.saveAndFlush(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void rechercheEtExports() {
        when(roleRepository.count(anyString())).thenReturn(2L);
        when(roleRepository.findPaginated(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(List.of(new Role(), new Role()));
        when(roleRepository.exportExcel(any(), any(), any(), any())).thenReturn("xls".getBytes());
        when(roleRepository.exportCsv(any(), any(), any(), any())).thenReturn(new StringBuilder("csv"));

        assertThat(controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse()).getTotal()).isEqualTo(2L);
        assertThat(controller.exportExcel("{}", "id", "asc", "{}", new MockHttpServletResponse()).getBody()).isNotEmpty();
        assertThat(controller.exportCsv("{}", "id", "asc", "{}", new MockHttpServletResponse()).getBody()).isEqualTo("csv");
    }

    @Test
    void creeUnRoleSiCodeEtLibelleLibres() throws Exception {
        when(roleRepository.exist(any(Role.class))).thenReturn(false);
        Role role = new Role();

        assertThat(controller.create(role)).isSameAs(role);

        when(roleRepository.exist(any(Role.class))).thenReturn(true);
        assertThatThrownBy(() -> controller.create(new Role()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("déjà existant");
    }

    @Test
    void updateExigeUnRoleExistant() {
        when(roleJpaRepository.findById(7)).thenReturn(new Role());
        when(roleRepository.exist(any(Role.class))).thenReturn(false);

        assertThat(controller.update(7, new Role())).isNotNull();

        when(roleJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.update(99, new Role())).isInstanceOf(AppException.class);
    }

    @Test
    void deleteRefuseSiDesUtilisateursOntLeRole() {
        Role role = new Role();
        role.setId(7);
        when(roleJpaRepository.findById(7)).thenReturn(role);
        when(utilisateurJpaRepository.countUserWithRole(7)).thenReturn(3L);

        assertThatThrownBy(() -> controller.delete(7))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        when(utilisateurJpaRepository.countUserWithRole(7)).thenReturn(0L);
        assertThat(controller.delete(7)).isTrue();
        verify(roleJpaRepository).delete(role);

        when(roleJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.delete(99)).isInstanceOf(AppException.class);
    }

    @Test
    void lesDroitsDUnRoleSontRecherchesParFonction() {
        Role role = new Role();
        AppFonction appFonction = new AppFonction();
        appFonction.setCode(AppFonctionEnum.CONVENTION);
        RoleAppFonction droit = new RoleAppFonction();
        droit.setAppFonction(appFonction);
        role.setRoleAppFonctions(List.of(droit));
        when(roleJpaRepository.findOneByCode("GES")).thenReturn(role);

        assertThat(controller.getRoleAppFonction("GES", "CONVENTION")).isSameAs(droit);

        assertThatThrownBy(() -> controller.getRoleAppFonction("GES", "FONCTION_INCONNUE"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> controller.getRoleAppFonction("GES", "NOMENCLATURE"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        when(roleJpaRepository.findOneByCode("ABSENT")).thenReturn(null);
        assertThatThrownBy(() -> controller.getRoleAppFonction("ABSENT", "CONVENTION"))
                .isInstanceOf(AppException.class);
    }

    @Test
    void getByIdEtAppFonctionsDeleguent() {
        Role role = new Role();
        when(roleJpaRepository.findById(7)).thenReturn(role);
        assertThat(controller.getById(7)).isSameAs(role);

        when(appFonctionJpaRepository.findAll()).thenReturn(List.of(new AppFonction()));
        assertThat(controller.getAppFonctions()).hasSize(1);
    }
}
