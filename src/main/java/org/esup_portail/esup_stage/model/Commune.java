package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Commune")
public class Commune {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCommune", nullable = false)
    private int id;

    @Column(name = "libelleCommune", nullable = false)
    private String libelle;

    @Column(name = "codePostal", nullable = false)
    private String codePostal;

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

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }
}
