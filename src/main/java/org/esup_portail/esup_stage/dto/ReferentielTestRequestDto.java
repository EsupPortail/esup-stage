package org.esup_portail.esup_stage.dto;

import lombok.Data;

@Data
public class ReferentielTestRequestDto {

    private String login;
    private String password;
    private String ldapUrl;
    private String apogeeUrl;

}
