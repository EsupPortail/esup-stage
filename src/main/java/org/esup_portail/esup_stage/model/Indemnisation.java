package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "Indemnisation")
public class Indemnisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idIndemnisation", nullable = false)
    private int id;

    @Column(name = "libelleIndemnisation", nullable = false)
    private String libelle;

    @Column(name = "temEnServIndem", nullable = false, length = 1)
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
