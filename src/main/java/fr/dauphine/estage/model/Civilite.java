package fr.dauphine.estage.model;

import javax.persistence.*;

@Entity
@Table(name = "Civilite")
public class Civilite {

    @Id
    @GeneratedValue
    @Column(name = "idCivilite", nullable = false)
    private int id;

    @Column(name = "libelleCivilite", nullable = false, length = 50)
    private String libelle;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}
