package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.esup_portail.esup_stage.constants.ValidationPatterns;

public class SendMailGroupeDto {
    @NotNull
    @NotEmpty
    private String templateMail;

    @NotNull
    private int conventionId;

    @NotNull
    @NotEmpty
    @Pattern(regexp = ValidationPatterns.EMAIL, message = "L'adresse mail n'est pas valide")
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
