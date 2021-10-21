package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "CritereGestion")
public class CritereGestion {

    @EmbeddedId
    private CritereGestionId id;

    @Column(name = "libelleCritere", nullable = false, length = 200)
    private String libelle;

    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    private CentreGestion centreGestion;

    public CritereGestionId getId() {
        return id;
    }

    public void setId(CritereGestionId id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public CentreGestion getCentreGestion() {
        return centreGestion;
    }

    public void setCentreGestion(CentreGestion centreGestion) {
        this.centreGestion = centreGestion;
    }
}
