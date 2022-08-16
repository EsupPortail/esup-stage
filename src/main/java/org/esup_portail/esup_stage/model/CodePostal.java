package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "CodePostal")
public class CodePostal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCodePostal", nullable = false)
    private int id;

    @Column(name = "libelleCodePostal", nullable = false)
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
