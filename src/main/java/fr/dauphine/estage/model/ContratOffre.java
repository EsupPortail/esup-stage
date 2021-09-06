package fr.dauphine.estage.model;

import javax.persistence.*;

@Entity
@Table(name = "ContratOffre")
public class ContratOffre {

    @Id
    @GeneratedValue
    @Column(name = "idContratOffre", nullable = false)
    private int id;

    @Column(name = "libelleContratOffre", nullable = false, length = 100)
    private String libelle;

    @Column(nullable = false, length = 20)
    private String codeCtrl;

    @Column(name = "temEnServContratOffre", nullable = false, length = 1)
    private String temEnServ;

    @ManyToOne
    @JoinColumn(name = "idTypeOffre", nullable = false)
    private TypeOffre typeOffre;

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

    public String getCodeCtrl() {
        return codeCtrl;
    }

    public void setCodeCtrl(String codeCtrl) {
        this.codeCtrl = codeCtrl;
    }

    public String getTemEnServ() {
        return temEnServ;
    }

    public void setTemEnServ(String temEnServ) {
        this.temEnServ = temEnServ;
    }

    public TypeOffre getTypeOffre() {
        return typeOffre;
    }

    public void setTypeOffre(TypeOffre typeOffre) {
        this.typeOffre = typeOffre;
    }

    public Boolean getModifiable() {
        return modifiable;
    }

    public void setModifiable(Boolean modifiable) {
        this.modifiable = modifiable;
    }
}
