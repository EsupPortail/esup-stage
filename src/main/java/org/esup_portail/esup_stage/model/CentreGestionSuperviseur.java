package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "CentreGestionSuperViseur")
public class CentreGestionSuperviseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCentreGestionSuperViseur", nullable = false)
    private int id;

    @Column(name = "nomCentreSuperViseur", nullable = false)
    private String nom;

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
}
