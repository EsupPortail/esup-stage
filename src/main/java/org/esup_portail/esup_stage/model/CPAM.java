package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CPAM")
public class CPAM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCPAM", nullable = false)
    private int id;

    @Column(name = "libelleCPAM", nullable = false)
    private String libelle;

    @Column(name = "region", nullable = false)
    private String region;

    @Column(name = "adresse", nullable = false)
    private String adresse;

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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
}
