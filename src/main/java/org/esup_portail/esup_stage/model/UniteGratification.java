package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "UniteGratification")
public class UniteGratification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUniteGratification", nullable = false)
    private int id;

    @Column(name = "libelleUniteGratification", nullable = false, length = 100)
    private String libelle;

    @Column(name = "temEnServGrat", nullable = false, length = 1)
    private String temEnServ;

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

    public String getTemEnServ() {
        return temEnServ;
    }

    public void setTemEnServ(String temEnServ) {
        this.temEnServ = temEnServ;
    }
}
