package fr.dauphine.estage.model;

import javax.persistence.*;

@Entity
@Table(name = "ModeValidationStage")
public class ModeValidationStage {

    @Id
    @GeneratedValue
    @Column(name = "idModeValidationStage", nullable = false)
    private int id;

    @Column(name = "libelleModeValidationStage", nullable = false, length = 100)
    private String libelle;

    @Column(name = "temEnServModeValid", nullable = false, length = 1)
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
}
