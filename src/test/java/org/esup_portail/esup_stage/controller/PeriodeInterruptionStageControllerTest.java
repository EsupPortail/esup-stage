package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PeriodeInterruptionStageDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.PeriodeInterruptionStageJpaRepository;
import org.esup_portail.esup_stage.repository.PeriodeInterruptionStageRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PeriodeInterruptionStageControllerTest {

    private PeriodeInterruptionStageController controller;
    private PeriodeInterruptionStageJpaRepository periodeInterruptionStageJpaRepository;
    private PeriodeInterruptionStageRepository periodeInterruptionStageRepository;
    private ConventionJpaRepository conventionJpaRepository;

    @BeforeEach
    void setUp() {
        controller = new PeriodeInterruptionStageController();
        periodeInterruptionStageJpaRepository = mock(PeriodeInterruptionStageJpaRepository.class);
        periodeInterruptionStageRepository = mock(PeriodeInterruptionStageRepository.class);
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        controller.periodeInterruptionStageJpaRepository = periodeInterruptionStageJpaRepository;
        controller.periodeInterruptionStageRepository = periodeInterruptionStageRepository;
        controller.conventionJpaRepository = conventionJpaRepository;

        when(periodeInterruptionStageJpaRepository.saveAndFlush(any(PeriodeInterruptionStage.class)))
                .thenAnswer(inv -> inv.getArgument(0));
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

    private PeriodeInterruptionStage interruptionDe(String identEtudiant) {
        PeriodeInterruptionStage interruption = new PeriodeInterruptionStage();
        interruption.setConvention(conventionDe(identEtudiant));
        return interruption;
    }

    @Test
    void laRechercheDUnEtudiantEstFiltreeSurSesConventions() {
        connecte("etu1", Role.ETU);
        when(periodeInterruptionStageRepository.count(anyString())).thenReturn(0L);
        when(periodeInterruptionStageRepository.findPaginated(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());

        controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse());

        ArgumentCaptor<String> filtres = ArgumentCaptor.forClass(String.class);
        verify(periodeInterruptionStageRepository).count(filtres.capture());
        assertThat(filtres.getValue()).contains("etu1");
    }

    @Test
    void getByIdEtGetByConventionProtegentLesDonnees() {
        PeriodeInterruptionStage interruption = interruptionDe("etu1");
        when(periodeInterruptionStageJpaRepository.findById(7)).thenReturn(interruption);
        when(conventionJpaRepository.findById(42)).thenReturn(conventionDe("etu1"));
        when(periodeInterruptionStageJpaRepository.findByConvention(42)).thenReturn(List.of(interruption));

        connecte("etu1", Role.ETU);
        assertThat(controller.getById(7)).isSameAs(interruption);
        assertThat(controller.getByConvention(42)).hasSize(1);

        connecte("autre", Role.ETU);
        assertThatThrownBy(() -> controller.getById(7)).isInstanceOf(AppException.class);
        assertThat(controller.getByConvention(42)).isEmpty();
    }

    @Test
    void createRemplitLInterruption() {
        connecte("ges1", Role.GES);
        when(conventionJpaRepository.findById(42)).thenReturn(conventionDe("etu1"));

        PeriodeInterruptionStageDto dto = new PeriodeInterruptionStageDto();
        dto.setIdConvention(42);
        dto.setDateDebutInterruption(new Date(1700000000000L));
        dto.setDateFinInterruption(new Date(1700100000000L));

        PeriodeInterruptionStage interruption = controller.create(dto);

        assertThat(interruption.getDateDebutInterruption()).isEqualTo(new Date(1700000000000L));
        assertThat(interruption.getConvention()).isNotNull();

        when(conventionJpaRepository.findById(99)).thenReturn(null);
        dto.setIdConvention(99);
        assertThatThrownBy(() -> controller.create(dto)).isInstanceOf(AppException.class);
    }

    @Test
    void updateDeleteEtDeleteParConvention() {
        connecte("ges1", Role.GES);
        PeriodeInterruptionStage interruption = interruptionDe("etu1");
        when(periodeInterruptionStageJpaRepository.findById(7)).thenReturn(interruption);
        when(conventionJpaRepository.findById(42)).thenReturn(interruption.getConvention());

        PeriodeInterruptionStageDto dto = new PeriodeInterruptionStageDto();
        dto.setIdConvention(42);
        dto.setDateDebutInterruption(new Date(1700000000000L));
        assertThat(controller.update(7, dto)).isNotNull();

        assertThat(controller.delete(7)).isTrue();
        verify(periodeInterruptionStageJpaRepository).delete(interruption);

        assertThat(controller.deleteByConvention(42)).isTrue();
        verify(periodeInterruptionStageJpaRepository).deleteByConvention(42);

        when(periodeInterruptionStageJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.update(99, dto)).isInstanceOf(AppException.class);
        assertThatThrownBy(() -> controller.delete(99)).isInstanceOf(AppException.class);
        when(conventionJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.deleteByConvention(99)).isInstanceOf(AppException.class);
    }
}
