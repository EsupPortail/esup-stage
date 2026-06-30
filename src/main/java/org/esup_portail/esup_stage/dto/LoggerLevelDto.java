package org.esup_portail.esup_stage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoggerLevelDto {
    private String name;
    private String configuredLevel;
    private String effectiveLevel;
}

