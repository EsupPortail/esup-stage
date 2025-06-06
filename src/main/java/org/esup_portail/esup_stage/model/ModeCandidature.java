package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ModeCandidature")
public class ModeCandidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idModeCandidature", nullable = false)
    private int id;

    @Column(name = "libelleModeCandidature", nullable = false)
    private String libelle;

    @Column(name = "temEnServModeCandidature", nullable = false, length = 1)
    private String temEnServ;

    @Column(nullable = false)
    private String codeCtrl;

    @ManyToMany(mappedBy = "modeCandidatures")
    private List<Offre> offres = new ArrayList<>();

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

    public String getCodeCtrl() {
        return codeCtrl;
    }

    public void setCodeCtrl(String codeCtrl) {
        this.codeCtrl = codeCtrl;
    }

    public List<Offre> getOffres() {
        return offres;
    }

    public void setOffres(List<Offre> offres) {
        this.offres = offres;
    }
}
