package org.esup_portail.esup_stage.service.signature.model;

import org.esup_portail.esup_stage.enums.SignataireEnum;

import java.util.Date;

public class Historique {
    private SignataireEnum typeSignataire;
    private Date dateDepot;
    private Date dateSignature;

    public SignataireEnum getTypeSignataire() {
        return typeSignataire;
    }

    public void setTypeSignataire(SignataireEnum typeSignataire) {
        this.typeSignataire = typeSignataire;
    }

    public Date getDateDepot() {
        return dateDepot;
    }

    public void setDateDepot(Date dateDepot) {
        this.dateDepot = dateDepot;
    }

    public Date getDateSignature() {
        return dateSignature;
    }

    public void setDateSignature(Date dateSignature) {
        this.dateSignature = dateSignature;
    }
}
