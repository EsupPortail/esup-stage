package org.esup_portail.esup_stage.model;

import javax.persistence.*;

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
