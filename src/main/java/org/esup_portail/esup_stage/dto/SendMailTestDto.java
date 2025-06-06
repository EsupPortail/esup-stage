package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class SendMailTestDto {
    @NotNull
    @NotEmpty
    private String templateMail;

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

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
