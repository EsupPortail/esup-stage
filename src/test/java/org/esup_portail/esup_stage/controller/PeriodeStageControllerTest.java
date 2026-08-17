package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PeriodeStageDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.PeriodeStageJpaRepository;
import org.esup_portail.esup_stage.repository.PeriodeStageRepository;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PeriodeStageControllerTest {

    private PeriodeStageController controller;
    private PeriodeStageJpaRepository periodeStageJpaRepository;
    private PeriodeStageRepository periodeStageRepository;
    private ConventionJpaRepository conventionJpaRepository;

    @BeforeEach
    void setUp() {
        controller = new PeriodeStageController();
        periodeStageJpaRepository = mock(PeriodeStageJpaRepository.class);
        periodeStageRepository = mock(PeriodeStageRepository.class);
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        controller.periodeStageJpaRepository = periodeStageJpaRepository;
        controller.periodeStageRepository = periodeStageRepository;
        controller.conventionJpaRepository = conventionJpaRepository;

        when(periodeStageJpaRepository.saveAndFlush(any(PeriodeStage.class))).thenAnswer(inv -> inv.getArgument(0));
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

    private Convention conventionDe(String identEtudiant) {
        Convention convention = new Convention();
        convention.setId(42);
        Etudiant etudiant = new Etudiant();
        etudiant.setIdentEtudiant(identEtudiant);
        convention.setEtudiant(etudiant);
        return convention;
    }

    private PeriodeStage periodeDe(String identEtudiant) {
        PeriodeStage periodeStage = new PeriodeStage();
        periodeStage.setConvention(conventionDe(identEtudiant));
        return periodeStage;
    }

    @Test
    void laRechercheDUnEtudiantEstRestreinteASesConventions() {
        connecte("etu1", Role.ETU);
        when(periodeStageRepository.count(anyString())).thenReturn(0L);
        when(periodeStageRepository.findPaginated(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());

        controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse());

        ArgumentCaptor<String> filtres = ArgumentCaptor.forClass(String.class);
        verify(periodeStageRepository).count(filtres.capture());
        assertThat(filtres.getValue()).contains("convention.etudiant.identEtudiant").contains("etu1");
    }

    @Test
    void laRechercheDUnGestionnaireNEstPasFiltree() {
        connecte("ges1", Role.GES);
        when(periodeStageRepository.count("{}")).thenReturn(3L);
        when(periodeStageRepository.findPaginated(anyInt(), anyInt(), anyString(), anyString(), eq("{}")))
                .thenReturn(List.of(new PeriodeStage()));

        var reponse = controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse());

        assertThat(reponse.getTotal()).isEqualTo(3L);
    }

    @Test
    void getByIdProtegeLesPeriodesDesAutresEtudiants() {
        PeriodeStage periode = periodeDe("etu1");
        when(periodeStageJpaRepository.findById(7)).thenReturn(periode);

        connecte("etu1", Role.ETU);
        assertThat(controller.getById(7)).isSameAs(periode);

        connecte("autre", Role.ETU);
        assertThatThrownBy(() -> controller.getById(7)).isInstanceOf(AppException.class);

        when(periodeStageJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.getById(99)).isInstanceOf(AppException.class);
    }

    @Test
    void getByConventionRenvoieVideSiNonAutorise() {
        connecte("autre", Role.ETU);
        when(conventionJpaRepository.findById(42)).thenReturn(conventionDe("etu1"));

        assertThat(controller.getByConvention(42)).isEmpty();

        connecte("etu1", Role.ETU);
        when(periodeStageJpaRepository.findByConvention(any(Convention.class)))
                .thenReturn(List.of(new PeriodeStage()));
        assertThat(controller.getByConvention(42)).hasSize(1);
    }

    @Test
    void createExigeUneConventionValide() {
        connecte("ges1", Role.GES);
        PeriodeStageDto dto = new PeriodeStageDto();
        assertThatThrownBy(() -> controller.create(dto))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("must not be null");

        dto.setIdConvention(42);
        when(conventionJpaRepository.findById((Integer) 42)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> controller.create(dto)).isInstanceOf(AppException.class);
    }

    @Test
    void createRemplitLaPeriodeDepuisLeDto() {
        connecte("ges1", Role.GES);
        Convention convention = conventionDe("etu1");
        when(conventionJpaRepository.findById((Integer) 42)).thenReturn(Optional.of(convention));

        PeriodeStageDto dto = new PeriodeStageDto();
        dto.setIdConvention(42);
        dto.setDateDebut(new Date(1700000000000L));
        dto.setDateFin(new Date(1700100000000L));
        dto.setNbHeuresJournalieres(7);

        PeriodeStage periode = controller.create(dto);

        assertThat(periode.getConvention()).isSameAs(convention);
        assertThat(periode.getNbHeuresJournalieres()).isEqualTo(7);
        assertThat(periode.getDateDebut()).isEqualTo(new Date(1700000000000L));
    }

    @Test
    void updateEtDeleteControlentLAcces() {
        connecte("ges1", Role.GES);
        PeriodeStage periode = periodeDe("etu1");
        when(periodeStageJpaRepository.findById(7)).thenReturn(periode);
        when(conventionJpaRepository.findById((Integer) 42)).thenReturn(Optional.of(periode.getConvention()));

        PeriodeStageDto dto = new PeriodeStageDto();
        dto.setIdConvention(42);
        dto.setNbHeuresJournalieres(8);
        assertThat(controller.update(7, dto).getNbHeuresJournalieres()).isEqualTo(8);

        assertThat(controller.delete(7)).isTrue();
        verify(periodeStageJpaRepository).delete(periode);

        when(periodeStageJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.update(99, dto)).isInstanceOf(AppException.class);
        assertThatThrownBy(() -> controller.delete(99)).isInstanceOf(AppException.class);
    }

    @Test
    void deleteByConventionSupprimeToutesLesPeriodes() {
        connecte("ges1", Role.GES);
        when(conventionJpaRepository.findById(42)).thenReturn(conventionDe("etu1"));

        assertThat(controller.deleteByConvention(42)).isTrue();
        verify(periodeStageJpaRepository).deleteByConvention(42);

        when(conventionJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.deleteByConvention(99)).isInstanceOf(AppException.class);
    }
}
