package org.esup_portail.esup_stage.dto;

import lombok.Data;

@Data
public class DocaposteTestRequestDto {

    private String uri;
    private String siren;
    private String keystorePath;
    private String keystorePassword;
    private String truststorePath;
    private String truststorePassword;
}
