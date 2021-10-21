package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "Confidentialite")
public class Confidentialite {

    @Id
    @Column(name = "codeConfidentialite", nullable = false)
    private String code;

    @Column(name = "libelleConfidentialite", nullable = false, length = 50)
    private String libelle;

    @Column(name = "temEnServConfid", nullable = false, length = 1)
    private String temEnServ;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
