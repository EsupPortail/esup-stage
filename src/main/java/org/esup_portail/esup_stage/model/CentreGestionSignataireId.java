package org.esup_portail.esup_stage.model;

import org.esup_portail.esup_stage.enums.SignataireEnum;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
