package fr.dauphine.estage.model;

import javax.persistence.*;

@Entity
@Table(name = "Ufr")
public class Ufr {

    @EmbeddedId
    private UfrId id;

    @Column(name = "libelleUFR", nullable = false, length = 100)
    private String libelle;

    public UfrId getId() {
        return id;
    }

    public void setId(UfrId id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}
