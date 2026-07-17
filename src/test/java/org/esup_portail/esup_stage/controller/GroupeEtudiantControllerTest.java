package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.GroupeEtudiantJpaRepository;
import org.esup_portail.esup_stage.repository.GroupeEtudiantRepository;
import org.esup_portail.esup_stage.repository.HistoriqueMailGroupeJpaRepository;
import org.esup_portail.esup_stage.repository.TypeConventionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroupeEtudiantControllerTest {

    private GroupeEtudiantController controller;
    private GroupeEtudiantRepository groupeEtudiantRepository;
    private GroupeEtudiantJpaRepository groupeEtudiantJpaRepository;
    private HistoriqueMailGroupeJpaRepository historiqueMailGroupeJpaRepository;
    private TypeConventionJpaRepository typeConventionJpaRepository;
    private ConventionJpaRepository conventionJpaRepository;

    @BeforeEach
    void setUp() {
        controller = new GroupeEtudiantController();
        groupeEtudiantRepository = mock(GroupeEtudiantRepository.class);
        groupeEtudiantJpaRepository = mock(GroupeEtudiantJpaRepository.class);
        historiqueMailGroupeJpaRepository = mock(HistoriqueMailGroupeJpaRepository.class);
        typeConventionJpaRepository = mock(TypeConventionJpaRepository.class);
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        controller.groupeEtudiantRepository = groupeEtudiantRepository;
        controller.groupeEtudiantJpaRepository = groupeEtudiantJpaRepository;
        controller.historiqueMailGroupeJpaRepository = historiqueMailGroupeJpaRepository;
        controller.typeConventionJpaRepository = typeConventionJpaRepository;
        controller.conventionJpaRepository = conventionJpaRepository;
    }

    @Test
    void searchDelegueAuRepositoryPagine() {
        when(groupeEtudiantRepository.count(anyString())).thenReturn(1L);
        when(groupeEtudiantRepository.findPaginated(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(List.of(new GroupeEtudiant()));

        var reponse = controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse());

        assertThat(reponse.getTotal()).isEqualTo(1L);
        assertThat(reponse.getData()).hasSize(1);
    }

    @Test
    void getByIdEchoueSiInconnu() {
        when(groupeEtudiantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.getById(99)).isInstanceOf(AppException.class);

        GroupeEtudiant groupe = new GroupeEtudiant();
        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(groupe);
        assertThat(controller.getById(7)).isSameAs(groupe);
    }

    @Test
    void historiqueDesMailsDuGroupe() {
        when(historiqueMailGroupeJpaRepository.findByGroupeEtudiant(7)).thenReturn(List.of());

        assertThat(controller.getHistorique(7)).isEmpty();
    }

    @Test
    void leTypeDeConventionEstPropageATousLesEtudiants() {
        GroupeEtudiant groupe = new GroupeEtudiant();
        Convention conventionGroupe = new Convention();
        groupe.setConvention(conventionGroupe);
        EtudiantGroupeEtudiant etudiantGroupe = new EtudiantGroupeEtudiant();
        Convention conventionEtudiant = new Convention();
        etudiantGroupe.setConvention(conventionEtudiant);
        groupe.setEtudiantGroupeEtudiants(List.of(etudiantGroupe));
        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(groupe);
        TypeConvention type = new TypeConvention();
        when(typeConventionJpaRepository.findById(3)).thenReturn(type);

        controller.setTypeConventionGroupe(7, 3);

        assertThat(conventionGroupe.getTypeConvention()).isSameAs(type);
        assertThat(conventionEtudiant.getTypeConvention()).isSameAs(type);
        verify(conventionJpaRepository).flush();
    }

    @Test
    void leTypeDeConventionExigeGroupeEtTypeExistants() {
        when(groupeEtudiantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.setTypeConventionGroupe(99, 3)).isInstanceOf(AppException.class);

        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(new GroupeEtudiant());
        when(typeConventionJpaRepository.findById(88)).thenReturn(null);
        assertThatThrownBy(() -> controller.setTypeConventionGroupe(7, 88)).isInstanceOf(AppException.class);
    }

    @Test
    void deleteSupprimeLeGroupeExistant() {
        GroupeEtudiant groupe = new GroupeEtudiant();
        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(groupe);

        assertThat(controller.delete(7)).isTrue();
        verify(groupeEtudiantJpaRepository).delete(groupe);

        when(groupeEtudiantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.delete(99)).isInstanceOf(AppException.class);
    }

    @Test
    void setInfosStageValidBasculeLeFlag() {
        GroupeEtudiant groupe = new GroupeEtudiant();
        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(groupe);
        when(groupeEtudiantJpaRepository.saveAndFlush(groupe)).thenReturn(groupe);

        assertThat(controller.setInfosStageValid(7, true).isInfosStageValid()).isTrue();
        assertThat(controller.setInfosStageValid(7, false).isInfosStageValid()).isFalse();

        when(groupeEtudiantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.setInfosStageValid(99, true)).isInstanceOf(AppException.class);
    }

    @Test
    void validateBrouillonValideLaCreation() {
        GroupeEtudiant groupe = new GroupeEtudiant();
        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(groupe);
        when(groupeEtudiantJpaRepository.saveAndFlush(groupe)).thenReturn(groupe);

        assertThat(controller.validateBrouillon(7).isValidationCreation()).isTrue();

        when(groupeEtudiantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.validateBrouillon(99)).isInstanceOf(AppException.class);
    }

    @Test
    void mergeObjectsPrivilegieLesValeursDuPremierObjet() throws IllegalAccessException {
        Etudiant premier = new Etudiant();
        premier.setNom("Durand");
        Etudiant second = new Etudiant();
        second.setNom("Ignoré");
        second.setPrenom("Alice");

        Etudiant fusion = GroupeEtudiantController.mergeObjects(premier, second);

        assertThat(fusion.getNom()).isEqualTo("Durand");
        assertThat(fusion.getPrenom()).isEqualTo("Alice");
    }
}
