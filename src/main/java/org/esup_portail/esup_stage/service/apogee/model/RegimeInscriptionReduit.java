package org.esup_portail.esup_stage.service.apogee.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Régime d'inscription tel que renvoyé par ESUP-SISCOL sur /regimesInscriptions
 * depuis le passage de l'endpoint d'une Map code/libellé à une liste d'objets.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegimeInscriptionReduit {
    private String codeRegimeInscription;
    private String libelleRegimeInscription;
    private String libelleRegimeInscriptionCourt;

    public String getCodeRegimeInscription() {
        return codeRegimeInscription;
    }

    public void setCodeRegimeInscription(String codeRegimeInscription) {
        this.codeRegimeInscription = codeRegimeInscription;
    }

    public String getLibelleRegimeInscription() {
        return libelleRegimeInscription;
    }

    public void setLibelleRegimeInscription(String libelleRegimeInscription) {
        this.libelleRegimeInscription = libelleRegimeInscription;
    }

    public String getLibelleRegimeInscriptionCourt() {
        return libelleRegimeInscriptionCourt;
    }

    public void setLibelleRegimeInscriptionCourt(String libelleRegimeInscriptionCourt) {
        this.libelleRegimeInscriptionCourt = libelleRegimeInscriptionCourt;
    }
}
