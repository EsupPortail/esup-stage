package org.esup_portail.esup_stage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "FAP_N1")
public class FapN1 {

    @Id
    @Column(name = "codeFAP_N1", nullable = false)
    private String code;

    @Column(nullable = false)
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
