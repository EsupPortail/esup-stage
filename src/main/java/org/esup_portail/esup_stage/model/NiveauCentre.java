package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;

@Entity
@Table(name = "NiveauCentre")
public class NiveauCentre implements Exportable {

    public static final String UFR = "UFR";
    public static final String ETAPE = "ETAPE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idNiveauCentre", nullable = false)
    private int id;

    @Column(name = "libelleNiveauCentre", nullable = false)
    private String libelle;

    @Column(name = "temEnServNiveauCentre", nullable = false, length = 1)
    private String temEnServ;

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

    public String getTemEnServ() {
        return temEnServ;
    }

    public void setTemEnServ(String temEnServ) {
        this.temEnServ = temEnServ;
    }

    @Override
    public String getExportValue(String key) {
        return null;
    }
}
