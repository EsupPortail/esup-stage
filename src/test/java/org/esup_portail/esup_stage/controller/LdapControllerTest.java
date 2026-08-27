package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.LdapSearchDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.EtudiantSecurityService;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du contrôleur LDAP : validation des filtres obligatoires, routage vers
 * le bon annuaire (/etudiant, /tuteur, /staff) et filtrage des étudiants selon le centre
 * de gestion du gestionnaire connecté.
 */
class LdapControllerTest {

    private LdapController controller;
    private LdapService ldapService;
    private EtudiantSecurityService etudiantSecurityService;

    @BeforeEach
    void setUp() {
        controller = new LdapController();
        ldapService = mock(LdapService.class);
        etudiantSecurityService = mock(EtudiantSecurityService.class);
        controller.ldapService = ldapService;
        controller.etudiantSecurityService = etudiantSecurityService;
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

    private LdapSearchDto avecNom(String nom) {
        LdapSearchDto dto = new LdapSearchDto();
        dto.setNom(nom);
        return dto;
    }

    // ---------------- getLdapUsers (/etudiants) ----------------

    @Test
    void getLdapUsersRefuseUneRechercheSansAucunFiltre() {
        connecte("ges1", Role.GES);
        assertThatThrownBy(() -> controller.getLdapUsers(new LdapSearchDto()))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void getLdapUsersNonGestionnaireRenvoieDirectementLaRecherche() {
        connecte("adm1", Role.ADM);
        when(etudiantSecurityService.isGestionnaireOrResponsableGestionnaire(any())).thenReturn(false);
        LdapUser etudiant = new LdapUser();
        when(ldapService.search(eq("/etudiant"), any())).thenReturn(List.of(etudiant));

        List<LdapUser> resultat = controller.getLdapUsers(avecNom("Dupont"));

        assertThat(resultat).containsExactly(etudiant);
    }

    @Test
    void getLdapUsersGestionnaireRenvoieVideSiRechercheHorsSonCentre() {
        connecte("ges1", Role.GES);
        when(etudiantSecurityService.isGestionnaireOrResponsableGestionnaire(any())).thenReturn(true);
        when(etudiantSecurityService.getIdsCentresGestionUtilisateur(any())).thenReturn(List.of(1));
        when(etudiantSecurityService.getCriteresCentresGestionUtilisateur(anyList())).thenReturn(List.of());
        when(etudiantSecurityService.isRechercheLdapEtudiantWithCentreGestionCriteria(any())).thenReturn(true);
        when(etudiantSecurityService.isRechercheLdapEtudiantInCentreGestionUtilisateur(any(), anyList())).thenReturn(false);

        List<LdapUser> resultat = controller.getLdapUsers(avecNom("Dupont"));

        assertThat(resultat).isEmpty();
    }

    @Test
    void getLdapUsersGestionnaireFiltreLesResultatsDeSonCentre() {
        connecte("ges1", Role.GES);
        when(etudiantSecurityService.isGestionnaireOrResponsableGestionnaire(any())).thenReturn(true);
        when(etudiantSecurityService.getIdsCentresGestionUtilisateur(any())).thenReturn(List.of(1));
        when(etudiantSecurityService.getCriteresCentresGestionUtilisateur(anyList())).thenReturn(List.of());
        when(etudiantSecurityService.isRechercheLdapEtudiantWithCentreGestionCriteria(any())).thenReturn(false);

        LdapUser dansCentre = new LdapUser();
        dansCentre.setUid("in");
        LdapUser horsCentre = new LdapUser();
        horsCentre.setUid("out");
        when(ldapService.search(eq("/etudiant"), any())).thenReturn(List.of(dansCentre, horsCentre));
        when(etudiantSecurityService.isLdapEtudiantInCentreGestionUtilisateur(any(), eq(dansCentre), anyList(), anyList())).thenReturn(true);
        when(etudiantSecurityService.isLdapEtudiantInCentreGestionUtilisateur(any(), eq(horsCentre), anyList(), anyList())).thenReturn(false);

        List<LdapUser> resultat = controller.getLdapUsers(avecNom("Dupont"));

        assertThat(resultat).containsExactly(dansCentre);
    }

    // ---------------- getLdapEnseignants (/tuteur) ----------------

    @Test
    void getLdapEnseignantsValideLesFiltresEtRouteVersTuteur() {
        assertThatThrownBy(() -> controller.getLdapEnseignants(new LdapSearchDto()))
                .isInstanceOf(AppException.class);

        LdapUser enseignant = new LdapUser();
        when(ldapService.search(eq("/tuteur"), any())).thenReturn(List.of(enseignant));
        assertThat(controller.getLdapEnseignants(avecNom("Martin"))).containsExactly(enseignant);
    }

    // ---------------- searchLdapUserByName (/staff) ----------------

    @Test
    void searchLdapUserByNameValideLesFiltresEtRouteVersStaff() {
        assertThatThrownBy(() -> controller.searchLdapUserByName(new LdapSearchDto()))
                .isInstanceOf(AppException.class);

        LdapUser staff = new LdapUser();
        when(ldapService.search(eq("/staff"), any())).thenReturn(List.of(staff));
        assertThat(controller.searchLdapUserByName(avecNom("Durand"))).containsExactly(staff);
    }

    // ---------------- searchLdapUserByLogin ----------------

    @Test
    void searchLdapUserByLoginRenvoieLUtilisateurOuUneListeVide() {
        LdapUser ldapUser = new LdapUser();
        when(ldapService.searchByLogin("jdupont")).thenReturn(ldapUser);
        assertThat(controller.searchLdapUserByLogin("jdupont")).containsExactly(ldapUser);

        when(ldapService.searchByLogin("inconnu")).thenReturn(null);
        assertThat(controller.searchLdapUserByLogin("inconnu")).isEmpty();
    }
}
