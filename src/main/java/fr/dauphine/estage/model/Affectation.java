package fr.dauphine.estage.model;

import javax.persistence.*;

@Entity
@Table(name = "Affectation")
public class Affectation {

    @EmbeddedId
    private AffectationId id;

    @Column(name = "libelleAffectation", length = 150)
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
