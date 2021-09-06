package fr.dauphine.estage.model;

import javax.persistence.*;

@Entity
@Table(name = "UniteDuree")
public class UniteDuree {

    @Id
    @GeneratedValue
    @Column(name = "idUniteDuree", nullable = false)
    private int id;

    @Column(name = "libelleUniteDuree", nullable = false, length = 100)
    private String libelle;

    @Column(name = "temEnServUniteDuree", nullable = false, length = 1)
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
}
