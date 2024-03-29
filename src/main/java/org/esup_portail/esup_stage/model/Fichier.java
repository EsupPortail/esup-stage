package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "Fichiers")
public class Fichier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFichier", nullable = false)
    private int id;

    @Column(name = "nomFichier", nullable = false)
    private String nom;

    @Lob
    private String nomReel;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNomReel() {
        return nomReel;
    }

    public void setNomReel(String nomReel) {
        this.nomReel = nomReel;
    }
}
