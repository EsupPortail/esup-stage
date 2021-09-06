package fr.dauphine.estage.model;

import javax.persistence.*;

@Entity
@Table(name = "Etape")
public class Etape {

    @EmbeddedId
    private EtapeId id;

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
