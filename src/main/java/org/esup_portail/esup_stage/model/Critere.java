package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Critere")
public class Critere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCritere", nullable = false)
    private int id;

    @Column(nullable = false)
    private String valeur;

    @Column(nullable = false)
    private String clef;

    @ManyToOne
    @JoinColumn(name = "niveau", nullable = false)
    private Niveau niveau;

    @ManyToOne
    @JoinColumn(name = "typeCategorie", nullable = false)
    private Categorie categorie;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public String getClef() {
        return clef;
    }

    public void setClef(String clef) {
        this.clef = clef;
    }

    public Niveau getNiveau() {
        return niveau;
    }

    public void setNiveau(Niveau niveau) {
        this.niveau = niveau;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }
}
