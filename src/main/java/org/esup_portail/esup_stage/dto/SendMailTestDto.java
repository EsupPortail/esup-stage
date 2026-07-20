package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.esup_portail.esup_stage.constants.ValidationPatterns;

public class SendMailTestDto {
    @NotNull
    @NotEmpty
    private String templateMail;

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

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
