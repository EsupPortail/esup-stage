package org.esup_portail.esup_stage.dto;

import lombok.Data;

@Data
public class AppPropertyDto {
    private String key;
    private String value;
    private Boolean isSecret;
    private Boolean hasValue;
}
