package fr.dauphine.estage.model;

import javax.persistence.*;

@Entity
@Table(name = "LangueConvention")
public class LangueConvention {

    @Id
    @Column(name = "codeLangueConvention", nullable = false)
    private String code;

    @Column(name = "libelleLangueConvention", nullable = false, length = 100)
    private String libelle;

    @Column(name = "temEnServLangue", length = 1)
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
