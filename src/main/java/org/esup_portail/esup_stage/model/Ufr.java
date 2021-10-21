package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.view.Views;

import javax.persistence.*;

@Entity
@Table(name = "Ufr")
public class Ufr {

    @JsonView(Views.List.class)
    @EmbeddedId
    private UfrId id;

    @JsonView(Views.List.class)
    @Column(name = "libelleUFR", nullable = false, length = 100)
    private String libelle;

    public UfrId getId() {
        return id;
    }

    public void setId(UfrId id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}