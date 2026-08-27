package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Clock;
import java.time.LocalDateTime;

@Entity
@Table(name = "Maintenance")
@Data
public class Maintenance implements Exportable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMaintenance", nullable = false)
    private Long id;

    @Column(name = "datDebMaint", nullable = false)
    private LocalDateTime datDebMaint;

    @Column(name = "datFinMaint")
    private LocalDateTime datFinMaint;

    @Column(name = "datAlertMaint")
    private LocalDateTime datAlertMaint;

    @Column(length = 500)
    private String message;

    @Column(name = "createdBy", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public boolean isActive() {
        return isActive(Clock.systemDefaultZone());
    }

    public boolean isActive(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        return !datDebMaint.isAfter(now) && (datFinMaint == null || datFinMaint.isAfter(now));
    }

    public boolean isUpcoming() {
        return isUpcoming(Clock.systemDefaultZone());
    }

    public boolean isUpcoming(Clock clock) {
        return datDebMaint.isAfter(LocalDateTime.now(clock));
    }

    public boolean isAlertActive() {
        return isAlertActive(Clock.systemDefaultZone());
    }

    public boolean isAlertActive(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        return datAlertMaint != null && !datAlertMaint.isAfter(now) && isUpcoming(clock);
    }

    public boolean isPassed() {
        return isPassed(Clock.systemDefaultZone());
    }

    public boolean isPassed(Clock clock) {
        return datFinMaint != null && !datFinMaint.isAfter(LocalDateTime.now(clock));
    }

    @Override
    public String getExportValue(String key) {
        return switch (key) {
            case "id" -> id != null ? id.toString() : "";
            case "datDebMaint" -> datDebMaint != null ? datDebMaint.toString() : "";
            case "datFinMaint" -> datFinMaint != null ? datFinMaint.toString() : "";
            case "datAlertMaint" -> datAlertMaint != null ? datAlertMaint.toString() : "";
            case "message" -> message != null ? message : "";
            case "createdBy" -> createdBy != null ? createdBy : "";
            case "createdAt" -> createdAt != null ? createdAt.toString() : "";
            default -> "";
        };
    }
}
