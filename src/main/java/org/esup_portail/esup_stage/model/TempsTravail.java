package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;

@Entity
@Table(name = "TempsTravail")
public class TempsTravail implements Exportable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTempsTravail", nullable = false)
    private int id;

    @Column(name = "libelleTempsTravail", nullable = false)
    private String libelle;

    @Column(nullable = false)
    private String codeCtrl;

    @Column(name = "temEnServTempsTravail", nullable = false, length = 1)
    private String temEnServ;

    private Boolean modifiable;

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

    public String getCodeCtrl() {
        return codeCtrl;
    }

    public void setCodeCtrl(String codeCtrl) {
        this.codeCtrl = codeCtrl;
    }

    public String getTemEnServ() {
        return temEnServ;
    }

    public void setTemEnServ(String temEnServ) {
        this.temEnServ = temEnServ;
    }

    public Boolean getModifiable() {
        return modifiable;
    }

    public void setModifiable(Boolean modifiable) {
        this.modifiable = modifiable;
    }

    @Override
    public String getExportValue(String key) {
        String value = "";
        switch (key) {
            case "codeCtrl":
                value = getCodeCtrl();
                break;
            case "libelle":
                value = getLibelle();
                break;
            case "actif":
                value = getTemEnServ().equals("O") ? "Oui" : "Non";
                break;
            default:
                break;
        }
        return value;
    }
}
