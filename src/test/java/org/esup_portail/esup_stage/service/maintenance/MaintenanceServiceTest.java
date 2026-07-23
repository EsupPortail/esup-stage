package org.esup_portail.esup_stage.service.maintenance;

import org.esup_portail.esup_stage.dto.MaintenanceStateDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Maintenance;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.MaintenanceJpaRepository;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaintenanceServiceTest {

    private MaintenanceService service;
    private MaintenanceJpaRepository maintenanceJpaRepository;
    private MaintenanceSseService maintenanceSseService;
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-17T10:00:00Z"), ZoneId.of("UTC"));
    private final LocalDateTime maintenant = LocalDateTime.now(Clock.fixed(Instant.parse("2026-07-17T10:00:00Z"), ZoneId.of("UTC")));

    @BeforeEach
    void setUp() {
        service = new MaintenanceService();
        maintenanceJpaRepository = mock(MaintenanceJpaRepository.class);
        maintenanceSseService = mock(MaintenanceSseService.class);
        ReflectionTestUtils.setField(service, "maintenanceJpaRepository", maintenanceJpaRepository);
        ReflectionTestUtils.setField(service, "maintenanceSseService", maintenanceSseService);
        ReflectionTestUtils.setField(service, "clock", clock);

        when(maintenanceJpaRepository.findActiveAt(any())).thenReturn(List.of());
        when(maintenanceJpaRepository.findUpcomingAt(any())).thenReturn(List.of());
        when(maintenanceJpaRepository.save(any(Maintenance.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Maintenance maintenance(LocalDateTime debut, LocalDateTime fin) {
        Maintenance maintenance = new Maintenance();
        maintenance.setDatDebMaint(debut);
        maintenance.setDatFinMaint(fin);
        maintenance.setMessage("Maintenance en cours");
        return maintenance;
    }

    @Test
    void lEtatInitialEstInactifQuandAucuneMaintenance() {
        service.initStateReference();

        assertThat(service.getLastKnownState().isActive()).isFalse();
        assertThat(service.isEffectiveMaintenanceActive()).isFalse();
    }

    @Test
    void lEtatInitialSurvitAUneBaseIndisponible() {
        when(maintenanceJpaRepository.findActiveAt(any())).thenThrow(new RuntimeException("db down"));

        service.initStateReference();

        assertThat(service.getLastKnownState().isActive()).isFalse();
    }

    @Test
    void uneMaintenanceEnCoursEstActive() {
        Maintenance active = maintenance(maintenant.minusHours(1), null);
        when(maintenanceJpaRepository.findActiveAt(any())).thenReturn(List.of(active));

        MaintenanceStateDto etat = service.getCurrentState();

        assertThat(etat.isActive()).isTrue();
        assertThat(etat.getMessage()).isEqualTo("Maintenance en cours");
    }

    @Test
    void uneMaintenanceAVenirDeclencheLAlerteQuandSaDateEstAtteinte() {
        Maintenance aVenir = maintenance(maintenant.plusHours(2), null);
        aVenir.setDatAlertMaint(maintenant.minusMinutes(30));
        when(maintenanceJpaRepository.findUpcomingAt(any())).thenReturn(List.of(aVenir));

        MaintenanceStateDto etat = service.getCurrentState();

        assertThat(etat.isActive()).isFalse();
        assertThat(etat.isUpcoming()).isTrue();
        assertThat(etat.isAlertActive()).isTrue();
    }

    @Test
    void activateDemarreLaMaintenanceImmediatement() {
        Maintenance maintenance = maintenance(maintenant.plusHours(2), maintenant.minusHours(1));
        when(maintenanceJpaRepository.findById(5L)).thenReturn(Optional.of(maintenance));

        Maintenance activee = service.activate(5L);

        assertThat(activee.getDatDebMaint()).isEqualTo(maintenant);
        assertThat(activee.getDatFinMaint()).isNull();
        verify(maintenanceSseService, atLeastOnce()).broadcast(any(MaintenanceStateDto.class));

        when(maintenanceJpaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.activate(99L)).isInstanceOf(AppException.class);
    }

    @Test
    void deactivateCloturelaMaintenance() {
        Maintenance maintenance = maintenance(maintenant.minusHours(1), null);
        when(maintenanceJpaRepository.findById(5L)).thenReturn(Optional.of(maintenance));

        assertThat(service.deactivate(5L).getDatFinMaint()).isEqualTo(maintenant);
    }

    @Test
    void saveCompleteLesInformationsDeCreation() {
        // création sans utilisateur connecté : créé par "system"
        Maintenance nouvelle = maintenance(maintenant.plusDays(1), null);
        Maintenance sauvee = service.save(nouvelle);
        assertThat(sauvee.getCreatedBy()).isEqualTo("system");
        assertThat(sauvee.getCreatedAt()).isEqualTo(maintenant);

        // création avec utilisateur connecté
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin("adm1");
        utilisateur.setUid("adm1");
        utilisateur.setRoles(List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CasUserDetailsImpl(utilisateur, List.of()), null));
        Maintenance autre = maintenance(maintenant.plusDays(1), null);
        assertThat(service.save(autre).getCreatedBy()).isEqualTo("adm1");

        // mise à jour : conserve les informations de création existantes
        Maintenance existante = maintenance(maintenant.minusDays(2), null);
        existante.setCreatedBy("createur");
        existante.setCreatedAt(maintenant.minusDays(3));
        when(maintenanceJpaRepository.findById(5L)).thenReturn(Optional.of(existante));
        Maintenance maj = maintenance(maintenant.plusDays(1), null);
        maj.setId(5L);
        Maintenance majSauvee = service.save(maj);
        assertThat(majSauvee.getCreatedBy()).isEqualTo("createur");
        assertThat(majSauvee.getCreatedAt()).isEqualTo(maintenant.minusDays(3));
    }

    @Test
    void saveValideLesDates() {
        assertThatThrownBy(() -> service.save(null)).isInstanceOf(AppException.class);

        Maintenance sansDebut = new Maintenance();
        assertThatThrownBy(() -> service.save(sansDebut))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("date de debut");

        Maintenance finAvantDebut = maintenance(maintenant, maintenant.minusHours(1));
        assertThatThrownBy(() -> service.save(finAvantDebut))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("posterieure");

        Maintenance alerteApresDebut = maintenance(maintenant, null);
        alerteApresDebut.setDatAlertMaint(maintenant.plusHours(1));
        assertThatThrownBy(() -> service.save(alerteApresDebut))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("alerte");
    }

    @Test
    void deleteEtTransitionsPlanifieesDiffusentLEtat() {
        Maintenance maintenance = maintenance(maintenant.minusHours(1), null);
        when(maintenanceJpaRepository.findById(5L)).thenReturn(Optional.of(maintenance));

        service.delete(5L);
        verify(maintenanceJpaRepository).delete(maintenance);

        when(maintenanceJpaRepository.findActiveAt(any())).thenReturn(List.of(maintenance));
        service.checkScheduledTransitions();
        assertThat(service.getLastKnownState().isActive()).isTrue();

        // une base en erreur ne fait pas planter la tâche planifiée
        when(maintenanceJpaRepository.findActiveAt(any())).thenThrow(new RuntimeException("db down"));
        service.checkScheduledTransitions();
    }

    @Test
    void findActiveEtFindAlertMaintenanceRetournentLaPremiere() {
        Maintenance maintenance = maintenance(maintenant.minusHours(1), null);
        when(maintenanceJpaRepository.findActiveAt(any())).thenReturn(List.of(maintenance));
        when(maintenanceJpaRepository.findAlertActiveUpcomingAt(any())).thenReturn(List.of());

        assertThat(service.findActive()).contains(maintenance);
        assertThat(service.findAlertMaintenance()).isEmpty();
    }
}
