package org.esup_portail.esup_stage.service.apogee.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdministrationApogee {
    private boolean statusApogee;
    private String raison;

    public boolean isStatusApogee() {
        return statusApogee;
    }

    public void setStatusApogee(boolean statusApogee) {
        this.statusApogee = statusApogee;
    }

    public String getRaison() {
        return raison;
    }

    public void setRaison(String raison) {
        this.raison = raison;
    }
}
