package fr.dauphine.estage.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.dauphine.estage.dto.view.Views;

import javax.persistence.*;

@Entity
@Table(name = "TypeStructure")
public class TypeStructure {

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTypeStructure", nullable = false)
    private int id;

    @JsonView(Views.List.class)
    @Column(name = "libelleTypeStructure", nullable = false, length = 100)
    private String libelle;

    @Column(name = "temEnServTypeStructure", nullable = false, length = 1)
    private String temEnServ;

    @Column(nullable = false)
    private boolean siretObligatoire;

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

    public String getTemEnServ() {
        return temEnServ;
    }

    public void setTemEnServ(String temEnServ) {
        this.temEnServ = temEnServ;
    }

    public boolean isSiretObligatoire() {
        return siretObligatoire;
    }

    public void setSiretObligatoire(boolean siretObligatoire) {
        this.siretObligatoire = siretObligatoire;
    }

    public Boolean getModifiable() {
        return modifiable;
    }

    public void setModifiable(Boolean modifiable) {
        this.modifiable = modifiable;
    }
}
