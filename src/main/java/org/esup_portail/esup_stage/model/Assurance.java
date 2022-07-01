package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "Assurance")
public class Assurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAssurance", nullable = false)
    private int id;

    @Column(name = "libelleAssurance", nullable = false)
    private String libelle;

    @Column(name = "temEnServAss", nullable = false, length = 1)
    private String temEnServ;

    @Column(nullable = false)
    private String codeCtrl;

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
}
