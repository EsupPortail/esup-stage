package org.esup_portail.esup_stage.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class SendMailGroupeDto {
    @NotNull
    @NotEmpty
    private String templateMail;

    @NotNull
    private int conventionId;

    @NotNull
    @NotEmpty
    @Email
    private String to;

    public String getTemplateMail() {
        return templateMail;
    }

    public void setTemplateMail(String templateMail) {
        this.templateMail = templateMail;
    }

    public int getConventionId() {
        return conventionId;
    }

    public void setConventionId(int conventionId) {
        this.conventionId = conventionId;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
