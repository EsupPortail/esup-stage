package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "ConsigneDocument")
public class ConsigneDocument extends ObjetMetier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idConsigneDocument", nullable = false)
    private int id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "idConsigne", nullable = false)
    private Consigne consigne;

    @Column(name = "nomDocument", nullable = false)
    private String nom;

    @Column(nullable = false)
    private String nomReel;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Consigne getConsigne() {
        return consigne;
    }

    public void setConsigne(Consigne consigne) {
        this.consigne = consigne;
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
