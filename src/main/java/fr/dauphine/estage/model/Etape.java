package fr.dauphine.estage.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.dauphine.estage.dto.view.Views;

import javax.persistence.*;

@Entity
@Table(name = "Etape")
public class Etape {

    @JsonView(Views.List.class)
    @EmbeddedId
    private EtapeId id;

    @JsonView(Views.List.class)
    @Column(name = "libelleEtape", nullable = false, length = 200)
    private String libelle;

    public EtapeId getId() {
        return id;
    }

    public void setId(EtapeId id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}
