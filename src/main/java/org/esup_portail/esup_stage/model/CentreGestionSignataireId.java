package org.esup_portail.esup_stage.model;

import org.esup_portail.esup_stage.enums.SignataireEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;

@Embeddable
public class CentreGestionSignataireId implements Serializable {

    @Column(nullable = false)
    private int idCentreGestion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignataireEnum signataire;

    public CentreGestionSignataireId(int idCentreGestion, SignataireEnum signataire) {
        this.idCentreGestion = idCentreGestion;
        this.signataire = signataire;
    }

    public CentreGestionSignataireId() {
    }

    public int getIdCentreGestion() {
        return idCentreGestion;
    }

    public void setIdCentreGestion(int idCentreGestion) {
        this.idCentreGestion = idCentreGestion;
    }

    public SignataireEnum getSignataire() {
        return signataire;
    }

    public void setSignataire(SignataireEnum signataire) {
        this.signataire = signataire;
    }
}
