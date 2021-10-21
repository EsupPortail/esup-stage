package org.esup_portail.esup_stage.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "FAP_N1")
public class FapN1 {

    @Id
    @Column(name = "codeFAP_N1", nullable = false, length = 1)
    private String code;

    @Column(nullable = false, length = 200)
    private String libelle;

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
}
