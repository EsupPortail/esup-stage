package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.model.AppFonction;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.RoleAppFonction;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.UtilisateurCentreGestionRole;
import org.esup_portail.esup_stage.repository.UtilisateurCentreGestionRoleJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests de la spécification "rôle par centre de gestion" au niveau du service d'habilitation :
 * - un utilisateur peut avoir des rôles différents selon le centre de gestion ;
 * - le comportement du rôle global est conservé ;
 * - le périmètre des centres (base du scope Responsable gestionnaire) est bien calculé.
 */
class HabilitationServiceTest {

    private HabilitationService habilitationService;
    private UtilisateurCentreGestionRoleJpaRepository utilisateurCentreGestionRoleJpaRepository;

    @BeforeEach
    void setUp() {
        habilitationService = new HabilitationService();
        utilisateurCentreGestionRoleJpaRepository = mock(UtilisateurCentreGestionRoleJpaRepository.class);
        ReflectionTestUtils.setField(habilitationService, "utilisateurCentreGestionRoleJpaRepository", utilisateurCentreGestionRoleJpaRepository);
    }

    // ----- Comportement du rôle global conservé -----

    @Test
    void hasGlobalRight_vrai_quand_le_role_global_accorde_le_droit() throws Exception {
        Utilisateur utilisateur = utilisateur(1, "ges01",
                role(Role.GES, AppFonctionEnum.CONVENTION, DroitEnum.VALIDATION));

        boolean result = habilitationService.hasGlobalRight(utilisateur,
                new AppFonctionEnum[]{AppFonctionEnum.CONVENTION}, new DroitEnum[]{DroitEnum.VALIDATION});

        assertThat(result).isTrue();
    }

    @Test
    void hasGlobalRight_faux_quand_le_role_global_n_accorde_pas_le_droit() throws Exception {
        Utilisateur utilisateur = utilisateur(1, "ens01",
                role(Role.ENS, AppFonctionEnum.CONVENTION, DroitEnum.LECTURE));

        boolean result = habilitationService.hasGlobalRight(utilisateur,
                new AppFonctionEnum[]{AppFonctionEnum.CONVENTION}, new DroitEnum[]{DroitEnum.MODIFICATION});

        assertThat(result).isFalse();
    }

    // ----- Un ou plusieurs rôles par centre de gestion -----

    @Test
    void hasCentreRight_utilise_le_role_du_centre() throws Exception {
        Utilisateur utilisateur = utilisateur(1, "u1", role(Role.ENS, AppFonctionEnum.CONVENTION, DroitEnum.LECTURE));
        Role gesCentre = role(Role.GES, AppFonctionEnum.CONVENTION, DroitEnum.MODIFICATION);
        when(utilisateurCentreGestionRoleJpaRepository.findByUtilisateurIdAndCentreGestionId(1, 5))
                .thenReturn(List.of(ucr(gesCentre, 5)));

        boolean result = habilitationService.hasCentreRight(utilisateur, 5,
                new AppFonctionEnum[]{AppFonctionEnum.CONVENTION}, new DroitEnum[]{DroitEnum.MODIFICATION});

        assertThat(result).isTrue();
    }

    @Test
    void hasCentreRight_faux_quand_le_role_du_centre_n_accorde_pas_le_droit() throws Exception {
        Utilisateur utilisateur = utilisateur(1, "u1", role(Role.GES, AppFonctionEnum.CONVENTION, DroitEnum.VALIDATION));
        // Sur ce centre l'utilisateur n'a que le rôle enseignant : pas de droit de modification
        Role ensCentre = role(Role.ENS, AppFonctionEnum.CONVENTION, DroitEnum.LECTURE);
        when(utilisateurCentreGestionRoleJpaRepository.findByUtilisateurIdAndCentreGestionId(1, 5))
                .thenReturn(List.of(ucr(ensCentre, 5)));

        boolean result = habilitationService.hasCentreRight(utilisateur, 5,
                new AppFonctionEnum[]{AppFonctionEnum.CONVENTION}, new DroitEnum[]{DroitEnum.MODIFICATION});

        assertThat(result).isFalse();
    }

    @Test
    void getEffectiveRoles_prend_le_role_du_centre_quand_il_existe() {
        Utilisateur utilisateur = utilisateur(1, "u1", role(Role.GES, AppFonctionEnum.CONVENTION, DroitEnum.VALIDATION));
        Role ensCentre = role(Role.ENS, AppFonctionEnum.CONVENTION, DroitEnum.LECTURE);
        when(utilisateurCentreGestionRoleJpaRepository.findByUtilisateurIdAndCentreGestionId(1, 5))
                .thenReturn(List.of(ucr(ensCentre, 5)));

        List<Role> effectifs = habilitationService.getEffectiveRoles(utilisateur, 5);

        assertThat(effectifs).extracting(Role::getCode).containsExactly(Role.ENS);
    }

    @Test
    void getEffectiveRoles_retombe_sur_le_role_global_quand_aucun_role_sur_le_centre() {
        Utilisateur utilisateur = utilisateur(1, "u1", role(Role.GES, AppFonctionEnum.CONVENTION, DroitEnum.VALIDATION));
        when(utilisateurCentreGestionRoleJpaRepository.findByUtilisateurIdAndCentreGestionId(1, 9))
                .thenReturn(new ArrayList<>());

        List<Role> effectifs = habilitationService.getEffectiveRoles(utilisateur, 9);

        assertThat(effectifs).extracting(Role::getCode).containsExactly(Role.GES);
    }

    // ----- Détection gestionnaire (global OU par centre) -----

    @Test
    void isGestionnaire_vrai_pour_un_gestionnaire_uniquement_par_centre() {
        Utilisateur utilisateur = utilisateur(1, "u1", role(Role.ENS, AppFonctionEnum.CONVENTION, DroitEnum.LECTURE));
        Role gesCentre = role(Role.GES, AppFonctionEnum.CONVENTION, DroitEnum.VALIDATION);
        when(utilisateurCentreGestionRoleJpaRepository.findByUtilisateurId(1))
                .thenReturn(List.of(ucr(gesCentre, 5)));

        assertThat(habilitationService.isGestionnaire(utilisateur)).isTrue();
    }

    @Test
    void isGestionnaire_faux_pour_un_enseignant_sans_role_gestionnaire_de_centre() {
        Utilisateur utilisateur = utilisateur(1, "u1", role(Role.ENS, AppFonctionEnum.CONVENTION, DroitEnum.LECTURE));
        Role ensCentre = role(Role.ENS, AppFonctionEnum.CONVENTION, DroitEnum.LECTURE);
        when(utilisateurCentreGestionRoleJpaRepository.findByUtilisateurId(1))
                .thenReturn(List.of(ucr(ensCentre, 5)));

        assertThat(habilitationService.isGestionnaire(utilisateur)).isFalse();
    }

    @Test
    void getGestionnaireCentreIds_ne_retient_que_les_centres_avec_role_gestionnaire() {
        Utilisateur utilisateur = utilisateur(1, "u1", role(Role.ENS, AppFonctionEnum.CONVENTION, DroitEnum.LECTURE));
        when(utilisateurCentreGestionRoleJpaRepository.findByUtilisateurId(1)).thenReturn(List.of(
                ucr(role(Role.RESP_GES, AppFonctionEnum.PARAM_CENTRE, DroitEnum.MODIFICATION), 5),
                ucr(role(Role.GES, AppFonctionEnum.CONVENTION, DroitEnum.VALIDATION), 7),
                ucr(role(Role.ENS, AppFonctionEnum.CONVENTION, DroitEnum.LECTURE), 9)
        ));

        List<Integer> centreIds = habilitationService.getGestionnaireCentreIds(utilisateur);

        assertThat(centreIds).containsExactlyInAnyOrder(5, 7);
    }

    // ----- Périmètre des centres (base du scope Responsable gestionnaire) -----

    @Test
    void getAuthorizedCentreIds_ne_retourne_que_les_centres_rattaches_qui_accordent_le_droit() {
        Utilisateur utilisateur = utilisateur(1, "resp01", role(Role.RESP_GES, AppFonctionEnum.PARAM_CENTRE, DroitEnum.LECTURE));
        when(utilisateurCentreGestionRoleJpaRepository.findByUtilisateurId(1)).thenReturn(List.of(
                ucr(role(Role.RESP_GES, AppFonctionEnum.PARAM_CENTRE, DroitEnum.LECTURE), 5),
                ucr(role(Role.RESP_GES, AppFonctionEnum.PARAM_CENTRE, DroitEnum.LECTURE), 7),
                // centre 9 : rôle enseignant, aucun droit PARAM_CENTRE -> hors périmètre
                ucr(role(Role.ENS, AppFonctionEnum.CONVENTION, DroitEnum.LECTURE), 9)
        ));

        List<Integer> centreIds = habilitationService.getAuthorizedCentreIds(utilisateur,
                new AppFonctionEnum[]{AppFonctionEnum.PARAM_CENTRE}, new DroitEnum[]{DroitEnum.LECTURE});

        assertThat(centreIds).containsExactlyInAnyOrder(5, 7);
    }

    // ----- fabriques -----

    private Utilisateur utilisateur(int id, String uid, Role... roles) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(id);
        utilisateur.setUid(uid);
        utilisateur.setLogin(uid);
        utilisateur.setRoles(new ArrayList<>(List.of(roles)));
        return utilisateur;
    }

    private Role role(String code, AppFonctionEnum fonction, DroitEnum... droits) {
        Role role = new Role();
        role.setCode(code);
        AppFonction appFonction = new AppFonction();
        appFonction.setCode(fonction);
        RoleAppFonction roleAppFonction = new RoleAppFonction();
        roleAppFonction.setAppFonction(appFonction);
        for (DroitEnum droit : droits) {
            switch (droit) {
                case LECTURE -> roleAppFonction.setLecture(true);
                case CREATION -> roleAppFonction.setCreation(true);
                case MODIFICATION -> roleAppFonction.setModification(true);
                case SUPPRESSION -> roleAppFonction.setSuppression(true);
                case VALIDATION -> roleAppFonction.setValidation(true);
            }
        }
        role.setRoleAppFonctions(new ArrayList<>(List.of(roleAppFonction)));
        return role;
    }

    private UtilisateurCentreGestionRole ucr(Role role, int centreId) {
        UtilisateurCentreGestionRole userCentreRole = new UtilisateurCentreGestionRole();
        userCentreRole.setRole(role);
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(centreId);
        userCentreRole.setCentreGestion(centreGestion);
        return userCentreRole;
    }
}
