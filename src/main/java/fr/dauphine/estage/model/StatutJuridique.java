package fr.dauphine.estage.model;

import javax.persistence.*;

@Entity
@Table(name = "StatutJuridique")
public class StatutJuridique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idStatutJuridique", nullable = false)
    private int id;

    @Column(name = "libelleStatutJuridique", nullable = false, length = 100)
    private String libelle;

    @Column(name = "temEnServStatut", nullable = false, length = 1)
    private String temEnServ;

    @ManyToOne
    @JoinColumn(name = "idTypeStructure", nullable = false)
    private TypeStructure typeStructure;

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

    public TypeStructure getTypeStructure() {
        return typeStructure;
    }

    public void setTypeStructure(TypeStructure typeStructure) {
        this.typeStructure = typeStructure;
    }

    public Boolean getModifiable() {
        return modifiable;
    }

    public void setModifiable(Boolean modifiable) {
        this.modifiable = modifiable;
    }
}
