package org.esup_portail.esup_stage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class OffreDiffusionId implements Serializable {

    @Column(nullable = false)
    private int idOffre;

    @Column(nullable = false)
    private int idCentreGestion;

    public int getIdOffre() {
        return idOffre;
    }

    public void setIdOffre(int idOffre) {
        this.idOffre = idOffre;
    }

    public int getIdCentreGestion() {
        return idCentreGestion;
    }

    public void setIdCentreGestion(int idCentreGestion) {
        this.idCentreGestion = idCentreGestion;
    }
}
