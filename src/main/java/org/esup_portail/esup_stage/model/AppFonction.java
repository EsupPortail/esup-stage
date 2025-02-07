package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;

@Entity
@Table(name = "AppFonction")
public class AppFonction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAppFonction", nullable = false)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "codeAppFonction", nullable = false, unique = true)
    private AppFonctionEnum code;

    @Column(name = "libelleAppFonction", nullable = false, unique = true)
    private String libelle;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AppFonctionEnum getCode() {
        return code;
    }

    public void setCode(AppFonctionEnum code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}
