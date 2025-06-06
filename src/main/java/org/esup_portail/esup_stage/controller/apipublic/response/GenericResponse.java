package org.esup_portail.esup_stage.controller.apipublic.response;

import jakarta.validation.constraints.NotNull;

public class GenericResponse {
    @NotNull
    private String message;

    public GenericResponse() {
        message = "OK";
    }

    public GenericResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
