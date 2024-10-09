package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;

@Entity
@Table(name = "OffreDiffusion")
public class OffreDiffusion {

    @EmbeddedId
    private OffreDiffusionId offreDiffusionId;

    @ManyToOne
    @MapsId("idOffre")
    @JoinColumn(name="idOffre")
    private Offre offre;

    @ManyToOne
    @MapsId("idCentreGestion")
    @JoinColumn(name="idCentreGestion")
    private CentreGestion centreGestion;

    @Column(nullable = false)
    private boolean estMiseEnAvant;

    public OffreDiffusionId getOffreDiffusionId() {
        return offreDiffusionId;
    }

    public void setOffreDiffusionId(OffreDiffusionId offreDiffusionId) {
        this.offreDiffusionId = offreDiffusionId;
    }

    public Offre getOffre() {
        return offre;
    }

    public void setOffre(Offre offre) {
        this.offre = offre;
    }

    public CentreGestion getCentreGestion() {
        return centreGestion;
    }

    public void setCentreGestion(CentreGestion centreGestion) {
        this.centreGestion = centreGestion;
    }

    public boolean isEstMiseEnAvant() {
        return estMiseEnAvant;
    }

    public void setEstMiseEnAvant(boolean estMiseEnAvant) {
        this.estMiseEnAvant = estMiseEnAvant;
    }
}
