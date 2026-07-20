package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PeriodeInterruptionAvenantDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.esup_portail.esup_stage.repository.PeriodeInterruptionAvenantJpaRepository;
import org.esup_portail.esup_stage.repository.PeriodeInterruptionAvenantRepository;
import org.esup_portail.esup_stage.repository.PeriodeInterruptionStageJpaRepository;
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

class PeriodeInterruptionAvenantControllerTest {

    private PeriodeInterruptionAvenantController controller;
    private PeriodeInterruptionAvenantRepository periodeInterruptionAvenantRepository;
    private PeriodeInterruptionAvenantJpaRepository periodeInterruptionAvenantJpaRepository;
    private PeriodeInterruptionStageJpaRepository periodeInterruptionStageJpaRepository;
    private AvenantJpaRepository avenantJpaRepository;

    @BeforeEach
    void setUp() {
        controller = new PeriodeInterruptionAvenantController();
        periodeInterruptionAvenantRepository = mock(PeriodeInterruptionAvenantRepository.class);
        periodeInterruptionAvenantJpaRepository = mock(PeriodeInterruptionAvenantJpaRepository.class);
        periodeInterruptionStageJpaRepository = mock(PeriodeInterruptionStageJpaRepository.class);
        avenantJpaRepository = mock(AvenantJpaRepository.class);
        controller.periodeInterruptionAvenantRepository = periodeInterruptionAvenantRepository;
        controller.periodeInterruptionAvenantJpaRepository = periodeInterruptionAvenantJpaRepository;
        controller.periodeInterruptionStageJpaRepository = periodeInterruptionStageJpaRepository;
        controller.avenantJpaRepository = avenantJpaRepository;

        when(periodeInterruptionAvenantJpaRepository.saveAndFlush(any(PeriodeInterruptionAvenant.class)))
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

    private Avenant avenantDe(String identEtudiant) {
        Avenant avenant = new Avenant();
        avenant.setId(9);
        Convention convention = new Convention();
        convention.setId(42);
        Etudiant etudiant = new Etudiant();
        etudiant.setIdentEtudiant(identEtudiant);
        convention.setEtudiant(etudiant);
        avenant.setConvention(convention);
        return avenant;
    }

    private PeriodeInterruptionAvenant interruptionDe(String identEtudiant) {
        PeriodeInterruptionAvenant periode = new PeriodeInterruptionAvenant();
        periode.setAvenant(avenantDe(identEtudiant));
        return periode;
    }

    @Test
    void laRechercheDUnEtudiantEstFiltreeSurSesAvenants() {
        connecte("etu1", Role.ETU);
        when(periodeInterruptionAvenantRepository.count(anyString())).thenReturn(0L);
        when(periodeInterruptionAvenantRepository.findPaginated(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());

        controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse());

        ArgumentCaptor<String> filtres = ArgumentCaptor.forClass(String.class);
        verify(periodeInterruptionAvenantRepository).count(filtres.capture());
        assertThat(filtres.getValue()).contains("etu1");
    }

    @Test
    void getByIdEtGetByAvenantProtegentLesDonnees() {
        PeriodeInterruptionAvenant interruption = interruptionDe("etu1");
        when(periodeInterruptionAvenantJpaRepository.findById(7)).thenReturn(interruption);
        when(avenantJpaRepository.findById(9)).thenReturn(avenantDe("etu1"));
        when(periodeInterruptionAvenantJpaRepository.findByAvenant(9)).thenReturn(List.of(interruption));

        connecte("etu1", Role.ETU);
        assertThat(controller.getById(7)).isSameAs(interruption);
        assertThat(controller.getByAvenant(9)).hasSize(1);

        connecte("autre", Role.ETU);
        assertThatThrownBy(() -> controller.getById(7)).isInstanceOf(AppException.class);
        assertThat(controller.getByAvenant(9)).isEmpty();

        when(periodeInterruptionAvenantJpaRepository.findById(99)).thenReturn(null);
        when(avenantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.getById(99)).isInstanceOf(AppException.class);
        assertThat(controller.getByAvenant(99)).isEmpty();
    }

    @Test
    void createRemplitLaPeriode() {
        connecte("ges1", Role.GES);
        when(avenantJpaRepository.findById(9)).thenReturn(avenantDe("etu1"));
        when(periodeInterruptionStageJpaRepository.findById(3)).thenReturn(new PeriodeInterruptionStage());

        PeriodeInterruptionAvenantDto dto = new PeriodeInterruptionAvenantDto();
        dto.setIdAvenant(9);
        dto.setIdPeriodeInterruptionStage(3);
        dto.setDateDebutInterruption(new Date(1700000000000L));
        dto.setDateFinInterruption(new Date(1700100000000L));

        PeriodeInterruptionAvenant periode = controller.create(dto);

        assertThat(periode.getDateDebutInterruption()).isEqualTo(new Date(1700000000000L));
        assertThat(periode.getAvenant()).isNotNull();
        assertThat(periode.getPeriodeInterruptionStage()).isNotNull();

        when(avenantJpaRepository.findById(99)).thenReturn(null);
        dto.setIdAvenant(99);
        assertThatThrownBy(() -> controller.create(dto)).isInstanceOf(AppException.class);
    }

    @Test
    void updateDeleteEtDeleteAll() {
        connecte("ges1", Role.GES);
        PeriodeInterruptionAvenant interruption = interruptionDe("etu1");
        when(periodeInterruptionAvenantJpaRepository.findById(7)).thenReturn(interruption);
        when(avenantJpaRepository.findById(9)).thenReturn(interruption.getAvenant());
        when(periodeInterruptionAvenantJpaRepository.findByAvenant(9)).thenReturn(List.of(interruption));

        PeriodeInterruptionAvenantDto dto = new PeriodeInterruptionAvenantDto();
        dto.setIdAvenant(9);
        dto.setDateDebutInterruption(new Date(1700000000000L));
        assertThat(controller.update(7, dto)).isNotNull();

        assertThat(controller.delete(7)).isTrue();
        verify(periodeInterruptionAvenantJpaRepository).delete(interruption);

        assertThat(controller.deleteAll(9)).isTrue();

        when(periodeInterruptionAvenantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.update(99, dto)).isInstanceOf(AppException.class);
        assertThatThrownBy(() -> controller.delete(99)).isInstanceOf(AppException.class);
        when(avenantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.deleteAll(99)).isInstanceOf(AppException.class);
    }
}
