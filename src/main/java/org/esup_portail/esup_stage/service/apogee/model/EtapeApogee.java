package org.esup_portail.esup_stage.service.apogee.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EtapeApogee {
    private String code;
    private String codeVrsEtp;
    private String libelle;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeVrsEtp() {
        return codeVrsEtp;
    }

    public void setCodeVrsEtp(String codeVrsEtp) {
        this.codeVrsEtp = codeVrsEtp;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}
