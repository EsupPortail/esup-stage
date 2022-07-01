package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "FAP_N2")
public class FapN2 {

    @Id
    @Column(name = "codeFAP_N2", nullable = false)
    private String code;

    @Column(nullable = false)
    private String libelle;

    @ManyToOne
    @JoinColumn(name = "codeFAP_N1", nullable = false)
    private FapN1 fapN1;

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

    public FapN1 getFapN1() {
        return fapN1;
    }

    public void setFapN1(FapN1 fapN1) {
        this.fapN1 = fapN1;
    }
}
