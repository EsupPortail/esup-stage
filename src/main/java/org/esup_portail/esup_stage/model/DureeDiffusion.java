package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "DureeDiffusion")
public class DureeDiffusion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDureeDiffusion", nullable = false)
    private int id;

    @Column(name = "libelleDureeDiffusion", nullable = false)
    private String libelle;

    @Column(nullable = false)
    private boolean adminSeulement;

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

    public boolean isAdminSeulement() {
        return adminSeulement;
    }

    public void setAdminSeulement(boolean adminSeulement) {
        this.adminSeulement = adminSeulement;
    }
}
