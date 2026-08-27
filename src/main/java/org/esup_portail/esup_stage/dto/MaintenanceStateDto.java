package org.esup_portail.esup_stage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceStateDto {
    private boolean active;
    private boolean upcoming;
    private boolean alertActive;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime alertDate;
    private String message;

    public static MaintenanceStateDto inactive() {
        return new MaintenanceStateDto(false, false, false, null, null, null, null);
    }
}
