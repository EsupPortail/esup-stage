package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.HabilitationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests du périmètre "Le responsable gestionnaire ne visualise que les centres de gestion
 * auxquels il est rattaché" au niveau de la lecture d'un centre par identifiant.
 */
class CentreGestionControllerTest {

    private CentreGestionController controller;
    private CentreGestionJpaRepository centreGestionJpaRepository;
    private HabilitationService habilitationService;

    @BeforeEach
    void setUp() {
        controller = new CentreGestionController();
        centreGestionJpaRepository = mock(CentreGestionJpaRepository.class);
        habilitationService = mock(HabilitationService.class);
        controller.centreGestionJpaRepository = centreGestionJpaRepository;
        controller.habilitationService = habilitationService;
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getById_autorise_le_responsable_gestionnaire_sur_un_centre_rattache() {
        setCurrentUser(utilisateur(Role.RESP_GES));
        when(habilitationService.hasCentreRoles(any(Utilisateur.class), eq(5))).thenReturn(true);
        CentreGestion centre = new CentreGestion();
        centre.setId(5);
        when(centreGestionJpaRepository.findById(5)).thenReturn(centre);

        CentreGestion result = controller.getById(5);

        assertThat(result.getId()).isEqualTo(5);
    }

    @Test
    void getById_refuse_le_responsable_gestionnaire_sur_un_centre_non_rattache() {
        setCurrentUser(utilisateur(Role.RESP_GES));
        when(habilitationService.hasCentreRoles(any(Utilisateur.class), eq(9))).thenReturn(false);

        assertThatThrownBy(() -> controller.getById(9))
                .isInstanceOf(AppException.class);
    }

    @Test
    void getById_autorise_l_administrateur_sur_n_importe_quel_centre() {
        setCurrentUser(utilisateur(Role.ADM));
        CentreGestion centre = new CentreGestion();
        centre.setId(9);
        when(centreGestionJpaRepository.findById(9)).thenReturn(centre);

        CentreGestion result = controller.getById(9);

        assertThat(result.getId()).isEqualTo(9);
    }

    // ----- utilitaires -----

    private void setCurrentUser(Utilisateur utilisateur) {
        CasUserDetailsImpl userDetails = new CasUserDetailsImpl(utilisateur, Collections.emptyList());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private Utilisateur utilisateur(String roleCode) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(1);
        utilisateur.setUid("u1");
        utilisateur.setLogin("u1");
        Role role = new Role();
        role.setCode(roleCode);
        role.setRoleAppFonctions(new ArrayList<>());
        utilisateur.setRoles(new ArrayList<>(List.of(role)));
        return utilisateur;
    }
}
