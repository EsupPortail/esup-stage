package org.esup_portail.esup_stage.dto;

import lombok.Data;

@Data
public class MailerTestRequestDto {

    private String protocol;
    private String host;
    private Integer port;
    private String auth;
    private String username;
    private String password;
    private String mailto;
    private String subject;
    private String content;

}
