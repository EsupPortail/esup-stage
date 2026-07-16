package org.esup_portail.esup_stage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveSessionDto {
    private String sessionId;
    private String login;
    private String nom;
    private String prenom;
    private List<String> roles;
    private Date lastRequest;
    private boolean current;
}
