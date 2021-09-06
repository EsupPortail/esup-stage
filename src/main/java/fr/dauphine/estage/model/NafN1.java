package fr.dauphine.estage.model;

import javax.persistence.*;

@Entity
@Table(name = "NAF_N1")
public class NafN1 {

    @Id
    @Column(name = "codeNAF_N1", nullable = false)
    private String code;

    @Column(name = "libelleNAF_N1", nullable = false, length = 150)
    private String libelle;

    @Column(name = "temEnServNAF_N1", length = 1)
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
