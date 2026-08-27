package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Etudiant;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.EtudiantJpaRepository;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.EtudiantSecurityService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EtudiantControllerTest {

    private EtudiantController controller;
    private EtudiantJpaRepository etudiantJpaRepository;

    @BeforeEach
    void setUp() {
        controller = new EtudiantController();
        etudiantJpaRepository = mock(EtudiantJpaRepository.class);
        controller.etudiantJpaRepository = etudiantJpaRepository;
        controller.etudiantSecurityService = new EtudiantSecurityService();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void connecte(String uid, String numEtudiant, String... roleCodes) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUid(uid);
        utilisateur.setLogin(uid);
        utilisateur.setNumEtudiant(numEtudiant);
        utilisateur.setRoles(java.util.Arrays.stream(roleCodes).map(code -> {
            Role role = new Role();
            role.setCode(code);
            return role;
        }).toList());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CasUserDetailsImpl(utilisateur, List.of()), null));
    }

    @Test
    void lesGestionnairesEtEnseignantsNAccedentPasAuxDonneesParLogin() {
        connecte("ges1", null, Role.GES);
        assertThatThrownBy(() -> controller.getByLogin("etu1"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        connecte("ens1", null, Role.ENS);
        assertThatThrownBy(() -> controller.getByLogin("etu1")).isInstanceOf(AppException.class);
    }

    @Test
    void unEtudiantNAccedeQuASesPropresDonnees() {
        Etudiant etudiant = new Etudiant();
        etudiant.setIdentEtudiant("etu1");
        when(etudiantJpaRepository.findByLogin("etu1")).thenReturn(etudiant);

        connecte("etu1", "123", Role.ETU);
        assertThat(controller.getByLogin("etu1")).isSameAs(etudiant);

        connecte("autre", "456", Role.ETU);
        assertThatThrownBy(() -> controller.getByLogin("etu1"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void unAdminAccedeATousLesEtudiants() {
        Etudiant etudiant = new Etudiant();
        etudiant.setIdentEtudiant("etu1");
        when(etudiantJpaRepository.findByLogin("etu1")).thenReturn(etudiant);

        connecte("adm1", null, Role.ADM);
        assertThat(controller.getByLogin("etu1")).isSameAs(etudiant);

        when(etudiantJpaRepository.findByLogin("inconnu")).thenReturn(null);
        assertThatThrownBy(() -> controller.getByLogin("inconnu"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
