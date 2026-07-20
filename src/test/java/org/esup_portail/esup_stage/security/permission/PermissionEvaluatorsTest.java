package org.esup_portail.esup_stage.security.permission;

import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.ContactJpaRepository;
import org.esup_portail.esup_stage.repository.ServiceJpaRepository;
import org.esup_portail.esup_stage.repository.StructureJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires des évaluateurs de permission utilisés par l'intercepteur {@code @Secure}.
 * Règle commune : un rôle non-étudiant est autorisé sans vérification de propriété ;
 * un étudiant n'accède qu'à ses propres ressources (délégation au repository {@code isOwner}).
 */
class PermissionEvaluatorsTest {

    private Utilisateur utilisateur(int id, String uid, String... roleCodes) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(id);
        utilisateur.setUid(uid);
        utilisateur.setRoles(java.util.Arrays.stream(roleCodes).map(code -> {
            Role role = new Role();
            role.setCode(code);
            return role;
        }).toList());
        return utilisateur;
    }

    private CentreGestion centre(int id) {
        CentreGestion centre = new CentreGestion();
        centre.setId(id);
        return centre;
    }

    // ---------------- StructurePermissionEvaluator ----------------

    @Test
    void structureAutoriseLesNonEtudiantsSansConsulterLeRepository() {
        StructurePermissionEvaluator evaluator = new StructurePermissionEvaluator();
        StructureJpaRepository repo = mock(StructureJpaRepository.class);
        ReflectionTestUtils.setField(evaluator, "structureJpaRepository", repo);

        boolean autorise = evaluator.hasPermission(utilisateur(1, "ges1", Role.GES), null, new Object[]{10});

        assertThat(autorise).isTrue();
        verify(repo, never()).isOwner(anyInt(), anyInt());
    }

    @Test
    void structureRestreintLEtudiantASesPropresStructures() {
        StructurePermissionEvaluator evaluator = new StructurePermissionEvaluator();
        StructureJpaRepository repo = mock(StructureJpaRepository.class);
        ReflectionTestUtils.setField(evaluator, "structureJpaRepository", repo);
        when(repo.isOwner(10, 7)).thenReturn(true);

        assertThat(evaluator.hasPermission(utilisateur(7, "etu1", Role.ETU), null, new Object[]{10})).isTrue();

        when(repo.isOwner(10, 7)).thenReturn(false);
        assertThat(evaluator.hasPermission(utilisateur(7, "etu1", Role.ETU), null, new Object[]{10})).isFalse();
    }

    // ---------------- ServicePermissionEvaluator ----------------

    @Test
    void serviceAppliqueLaMemeRegleQueStructure() {
        ServicePermissionEvaluator evaluator = new ServicePermissionEvaluator();
        ServiceJpaRepository repo = mock(ServiceJpaRepository.class);
        ReflectionTestUtils.setField(evaluator, "serviceJpaRepository", repo);

        assertThat(evaluator.hasPermission(utilisateur(1, "adm", Role.ADM), null, new Object[]{5})).isTrue();
        verify(repo, never()).isOwner(anyInt(), anyInt());

        when(repo.isOwner(5, 7)).thenReturn(true);
        assertThat(evaluator.hasPermission(utilisateur(7, "etu1", Role.ETU), null, new Object[]{5})).isTrue();
    }

    // ---------------- ContactPermissionEvaluator ----------------

    private ContactPermissionEvaluator contactEvaluator(ContactJpaRepository contactRepo, CentreGestionJpaRepository centreRepo) {
        ContactPermissionEvaluator evaluator = new ContactPermissionEvaluator();
        ReflectionTestUtils.setField(evaluator, "contactJpaRepository", contactRepo);
        ReflectionTestUtils.setField(evaluator, "centreGestionJpaRepository", centreRepo);
        return evaluator;
    }

    @Test
    void contactRestreintLEtudiantASesPropresContacts() {
        ContactJpaRepository contactRepo = mock(ContactJpaRepository.class);
        ContactPermissionEvaluator evaluator = contactEvaluator(contactRepo, mock(CentreGestionJpaRepository.class));
        when(contactRepo.isOwner(3, 7)).thenReturn(true);

        assertThat(evaluator.hasPermission(utilisateur(7, "etu1", Role.ETU), null, new Object[]{3})).isTrue();
    }

    @Test
    void contactAutoriseLeGestionnaireSurLesContactsDeSesCentres() {
        ContactJpaRepository contactRepo = mock(ContactJpaRepository.class);
        CentreGestionJpaRepository centreRepo = mock(CentreGestionJpaRepository.class);
        ContactPermissionEvaluator evaluator = contactEvaluator(contactRepo, centreRepo);
        when(centreRepo.findAllByGestionnaireUid("ges1")).thenReturn(List.of(centre(1), centre(2)));
        when(contactRepo.existsByIdAndCentreGestionIdIn(eq(3), eq(List.of(1, 2)))).thenReturn(true);

        assertThat(evaluator.hasPermission(utilisateur(9, "ges1", Role.GES), null, new Object[]{3})).isTrue();
    }

    @Test
    void contactRefuseLeGestionnaireSansUidOuSansCentre() {
        ContactJpaRepository contactRepo = mock(ContactJpaRepository.class);
        CentreGestionJpaRepository centreRepo = mock(CentreGestionJpaRepository.class);
        ContactPermissionEvaluator evaluator = contactEvaluator(contactRepo, centreRepo);

        // uid vide → refus immédiat, aucun accès au repository des centres
        assertThat(evaluator.hasPermission(utilisateur(9, "  ", Role.GES), null, new Object[]{3})).isFalse();
        verify(centreRepo, never()).findAllByGestionnaireUid(org.mockito.ArgumentMatchers.anyString());

        // uid présent mais aucun centre rattaché → refus
        when(centreRepo.findAllByGestionnaireUid("ges1")).thenReturn(List.of());
        assertThat(evaluator.hasPermission(utilisateur(9, "ges1", Role.RESP_GES), null, new Object[]{3})).isFalse();
    }

    @Test
    void contactAutoriseLesAutresRoles() {
        ContactPermissionEvaluator evaluator = contactEvaluator(mock(ContactJpaRepository.class), mock(CentreGestionJpaRepository.class));

        assertThat(evaluator.hasPermission(utilisateur(1, "ens1", Role.ENS), null, new Object[]{3})).isTrue();
    }
}
