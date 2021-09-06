package fr.dauphine.estage.model;

import javax.persistence.*;

@Entity
@Table(name = "Niveau")
public class Niveau {

    @Id
    @GeneratedValue
    @Column(name = "idNiveau", nullable = false)
    private int id;

    @Column(nullable = false)
    private int valeur;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getValeur() {
        return valeur;
    }

    public void setValeur(int valeur) {
        this.valeur = valeur;
    }
}
