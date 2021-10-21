package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "FAP_N3")
public class FapN3 {

    @Id
    @Column(name = "codeFAP_N3", nullable = false, length = 5)
    private String code;

    @Column(nullable = false, length = 200)
    private String libelle;

    @ManyToOne
    @JoinColumn(name = "codeFAP_N2", nullable = false)
    private FapN2 fapN2;

    @ManyToOne
    @JoinColumn(name = "numFAP_Qualification", nullable = false)
    private FapQualification fapQualification;

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

    public FapN2 getFapN2() {
        return fapN2;
    }

    public void setFapN2(FapN2 fapN2) {
        this.fapN2 = fapN2;
    }

    public FapQualification getFapQualification() {
        return fapQualification;
    }

    public void setFapQualification(FapQualification fapQualification) {
        this.fapQualification = fapQualification;
    }
}
