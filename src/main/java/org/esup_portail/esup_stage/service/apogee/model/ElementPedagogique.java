package org.esup_portail.esup_stage.service.apogee.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElementPedagogique {
    private String codEtp;
    private String codVrsVet;
    private String codElp;
    private String libElp;
    private String temElpTypeStage;
    private BigDecimal nbrCrdElp;
    private String LibNatureElp;

    public String getCodEtp() {
        return codEtp;
    }

    public void setCodEtp(String codEtp) {
        this.codEtp = codEtp;
    }

    public String getCodVrsVet() {
        return codVrsVet;
    }

    public void setCodVrsVet(String codVrsVet) {
        this.codVrsVet = codVrsVet;
    }

    public String getCodElp() {
        return codElp;
    }

    public void setCodElp(String codElp) {
        this.codElp = codElp;
    }

    public String getLibElp() {
        return libElp;
    }

    public void setLibElp(String libElp) {
        this.libElp = libElp;
    }

    public String getTemElpTypeStage() {
        return temElpTypeStage;
    }

    public void setTemElpTypeStage(String temElpTypeStage) {
        this.temElpTypeStage = temElpTypeStage;
    }

    public BigDecimal getNbrCrdElp() {
        return nbrCrdElp;
    }

    public void setNbrCrdElp(BigDecimal nbrCrdElp) {
        this.nbrCrdElp = nbrCrdElp;
    }

    public String getLibNatureElp() {
        return LibNatureElp;
    }

    public void setLibNatureElp(String libNatureElp) {
        LibNatureElp = libNatureElp;
    }
}
