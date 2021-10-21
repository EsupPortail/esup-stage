package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "CaisseRegime")
public class CaisseRegime {

    @Id
    @Column(name = "codeCaisse", nullable = false)
    private String code;

    @Column(name = "libelleCaisse", nullable = false, length = 100)
    private String libelle;

    @Column(name = "temEnServCaisse", nullable = false, length = 1)
    private String temEnServ;

    @Column(nullable = false, length = 100)
    private String infoCaisse;

    private Boolean modifiable;

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

    public String getInfoCaisse() {
        return infoCaisse;
    }

    public void setInfoCaisse(String infoCaisse) {
        this.infoCaisse = infoCaisse;
    }

    public Boolean getModifiable() {
        return modifiable;
    }

    public void setModifiable(Boolean modifiable) {
        this.modifiable = modifiable;
    }
}
