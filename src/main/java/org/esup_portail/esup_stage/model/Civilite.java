package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "Civilite")
public class Civilite implements Exportable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCivilite", nullable = false)
    private int id;

    @Column(name = "libelleCivilite", nullable = false, length = 50)
    private String libelle;

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

    @Override
    public String getExportValue(String key) {
        return null;
    }
}
