package fr.dauphine.estage.model;

import javax.persistence.*;

@Entity
@Table(name = "Categorie")
public class Categorie {

    @Id
    @GeneratedValue
    @Column(name = "idCategorie", nullable = false)
    private int id;

    @Column(name = "typeCategorie", nullable = false)
    private int type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
