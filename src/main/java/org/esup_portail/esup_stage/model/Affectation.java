package org.esup_portail.esup_stage.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "Affectation")
public class Affectation {

    @EmbeddedId
    private AffectationId id;

    @Column(name = "libelleAffectation")
    private String libelle;

    public AffectationId getId() {
        return id;
    }

    public void setId(AffectationId id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}
