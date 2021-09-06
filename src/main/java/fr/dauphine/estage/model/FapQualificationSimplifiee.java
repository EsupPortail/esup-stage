package fr.dauphine.estage.model;

import javax.persistence.*;

@Entity
@Table(name = "FAP_QualificationSimplifiee")
public class FapQualificationSimplifiee {

    @Id
    @GeneratedValue
    @Column(name = "idQualificationSimplifiee", nullable = false)
    private int id;

    @Column(name = "libelleQualification", length = 100)
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
