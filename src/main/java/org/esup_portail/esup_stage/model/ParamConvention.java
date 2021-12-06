package org.esup_portail.esup_stage.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ParamConvention")
public class ParamConvention {

    @Id
    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 150)
    private String libelle;

    @Column(length = 200)
    private String exemple;

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

    public String getExemple() {
        return exemple;
    }

    public void setExemple(String exemple) {
        this.exemple = exemple;
    }
}
