package org.esup_portail.esup_stage.service.maintenance;

import org.esup_portail.esup_stage.dto.MaintenanceStateDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Maintenance;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.MaintenanceJpaRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceService.class);

    @Autowired
    private MaintenanceJpaRepository maintenanceJpaRepository;

    @Autowired
    private MaintenanceSseService maintenanceSseService;

    @Autowired
    private Clock clock;

    private final AtomicReference<MaintenanceStateDto> lastBroadcastState = new AtomicReference<>(MaintenanceStateDto.inactive());

    @PostConstruct
    public void initStateReference() {
        try {
            lastBroadcastState.set(getCurrentState());
        } catch (RuntimeException e) {
            log.warn("Unable to initialize maintenance state from database: {}", e.getMessage());
            lastBroadcastState.set(MaintenanceStateDto.inactive());
        }
    }

    public MaintenanceStateDto getLastKnownState() {
        MaintenanceStateDto state = lastBroadcastState.get();
        return state != null ? state : MaintenanceStateDto.inactive();
    }

    public MaintenanceStateDto getCurrentState() {
        Optional<Maintenance> activeMaintenance = findActive().filter(maintenance -> maintenance.isActive(clock));
        if (activeMaintenance.isPresent()) {
            Maintenance maintenance = activeMaintenance.get();
            return toState(maintenance, true, false, false);
        }

        Optional<Maintenance> upcomingMaintenance = findUpcoming();
        if (upcomingMaintenance.isPresent()) {
            Maintenance maintenance = upcomingMaintenance.get();
            boolean alertActive = maintenance.isAlertActive(clock);
            return toState(maintenance, false, true, alertActive);
        }

        return MaintenanceStateDto.inactive();
    }

    public Maintenance activate(Long id) {
        Maintenance maintenance = findById(id);
        LocalDateTime now = now();

        maintenance.setDatDebMaint(now);
        if (maintenance.getDatFinMaint() != null && !maintenance.getDatFinMaint().isAfter(now)) {
            maintenance.setDatFinMaint(null);
        }

        Maintenance saved = maintenanceJpaRepository.save(maintenance);
        broadcastCurrentState(true);
        return saved;
    }

    public Maintenance deactivate(Long id) {
        Maintenance maintenance = findById(id);
        maintenance.setDatFinMaint(now());

        Maintenance saved = maintenanceJpaRepository.save(maintenance);
        broadcastCurrentState(true);
        return saved;
    }

    public Maintenance save(Maintenance maintenance) {
        validate(maintenance);

        if (maintenance.getId() == null) {
            if (!StringUtils.hasText(maintenance.getCreatedBy())) {
                maintenance.setCreatedBy(resolveCurrentLogin());
            }
            if (maintenance.getCreatedAt() == null) {
                maintenance.setCreatedAt(now());
            }
        } else {
            Maintenance existing = findById(maintenance.getId());
            if (!StringUtils.hasText(maintenance.getCreatedBy())) {
                maintenance.setCreatedBy(existing.getCreatedBy());
            }
            if (maintenance.getCreatedAt() == null) {
                maintenance.setCreatedAt(existing.getCreatedAt());
            }
        }

        Maintenance saved = maintenanceJpaRepository.save(maintenance);
        broadcastCurrentState(true);
        return saved;
    }

    public void delete(Long id) {
        Maintenance maintenance = findById(id);
        maintenanceJpaRepository.delete(maintenance);
        broadcastCurrentState(true);
    }

    public Optional<Maintenance> findActive() {
        return first(maintenanceJpaRepository.findActiveAt(now()));
    }

    public boolean isEffectiveMaintenanceActive() {
        return getLastKnownState().isActive();
    }

    public Optional<Maintenance> findAlertMaintenance() {
        return first(maintenanceJpaRepository.findAlertActiveUpcomingAt(now()));
    }

    @Scheduled(fixedDelayString = "${maintenance.scheduler.fixed-delay-ms:5000}")
    public void checkScheduledTransitions() {
        try {
            broadcastCurrentState(false);
        } catch (RuntimeException e) {
            log.warn("Maintenance state refresh failed: {}", e.getMessage());
        }
    }

    private Optional<Maintenance> findUpcoming() {
        return first(maintenanceJpaRepository.findUpcomingAt(now()));
    }

    private Optional<Maintenance> first(List<Maintenance> maintenances) {
        if (maintenances == null || maintenances.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(maintenances.get(0));
    }

    private Maintenance findById(Long id) {
        return maintenanceJpaRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Maintenance non trouvee"));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private void validate(Maintenance maintenance) {
        if (maintenance == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Maintenance invalide");
        }
        if (maintenance.getDatDebMaint() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La date de debut de maintenance est obligatoire");
        }
        if (maintenance.getDatFinMaint() != null && !maintenance.getDatFinMaint().isAfter(maintenance.getDatDebMaint())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La date de fin doit etre posterieure a la date de debut");
        }
        if (maintenance.getDatAlertMaint() != null && maintenance.getDatAlertMaint().isAfter(maintenance.getDatDebMaint())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La date d'alerte doit etre anterieure ou egale a la date de debut");
        }
    }

    private String resolveCurrentLogin() {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (utilisateur != null && StringUtils.hasText(utilisateur.getLogin())) {
            return utilisateur.getLogin();
        }
        return "system";
    }

    private MaintenanceStateDto toState(Maintenance maintenance, boolean active, boolean upcoming, boolean alertActive) {
        return new MaintenanceStateDto(
                active,
                upcoming,
                alertActive,
                maintenance.getDatDebMaint(),
                maintenance.getDatFinMaint(),
                maintenance.getDatAlertMaint(),
                maintenance.getMessage()
        );
    }

    private void broadcastCurrentState(boolean force) {
        MaintenanceStateDto state = getCurrentState();

        if (force) {
            lastBroadcastState.set(state);
            safeBroadcast(state);
            return;
        }

        if (updateLastStateIfChanged(state)) {
            safeBroadcast(state);
        }
    }

    private void safeBroadcast(MaintenanceStateDto state) {
        try {
            maintenanceSseService.broadcast(state);
        } catch (RuntimeException e) {
            log.warn("Maintenance SSE broadcast failed: {}", e.getMessage());
        }
    }

    private boolean updateLastStateIfChanged(MaintenanceStateDto newState) {
        while (true) {
            MaintenanceStateDto currentState = lastBroadcastState.get();
            if (Objects.equals(currentState, newState)) {
                return false;
            }
            if (lastBroadcastState.compareAndSet(currentState, newState)) {
                return true;
            }
        }
    }
}
