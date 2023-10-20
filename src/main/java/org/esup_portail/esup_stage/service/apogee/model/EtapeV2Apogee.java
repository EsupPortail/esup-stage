package org.esup_portail.esup_stage.service.apogee.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EtapeV2Apogee {
    private String codeEtp;
    private String codVrsVet;
    private String libWebVet;

    public String getCodeEtp() {
        return codeEtp;
    }

    public void setCodeEtp(String codeEtp) {
        this.codeEtp = codeEtp;
    }

    public String getCodVrsVet() {
        return codVrsVet;
    }

    public void setCodVrsVet(String codVrsVet) {
        this.codVrsVet = codVrsVet;
    }

    public String getLibWebVet() {
        return libWebVet;
    }

    public void setLibWebVet(String libWebVet) {
        this.libWebVet = libWebVet;
    }
}
