package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "FAP_Qualification")
public class FapQualification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "numFAP_Qualification", nullable = false)
    private int id;

    @Column(name = "libelleQualification", nullable = false, length = 100)
    private String libelle;

    @ManyToOne
    @JoinColumn(name = "idQualificationSimplifiee", nullable = false)
    private FapQualificationSimplifiee fapQualificationSimplifiee;

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

    public FapQualificationSimplifiee getFapQualificationSimplifiee() {
        return fapQualificationSimplifiee;
    }

    public void setFapQualificationSimplifiee(FapQualificationSimplifiee fapQualificationSimplifiee) {
        this.fapQualificationSimplifiee = fapQualificationSimplifiee;
    }
}
