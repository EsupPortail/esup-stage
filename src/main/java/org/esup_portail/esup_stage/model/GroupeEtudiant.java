package org.esup_portail.esup_stage.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "GroupeEtudiant")
public class GroupeEtudiant extends ObjetMetier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idGroupeEtudiant", nullable = false)
    private int id;

    @Column(name = "nom",nullable = false, length = 100)
    private String nom;

    @OneToMany(mappedBy = "groupeEtudiant", fetch = FetchType.EAGER, cascade = {CascadeType.MERGE}, orphanRemoval = true)
    private List<EtudiantGroupeEtudiant> etudiantGroupeEtudiants;

    @OneToOne
    @JoinColumn(name = "idConvention", nullable = false)
    private Convention convention;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<EtudiantGroupeEtudiant> getEtudiantGroupeEtudiants() {
        return etudiantGroupeEtudiants;
    }

    public void setEtudiantGroupeEtudiants(List<EtudiantGroupeEtudiant> etudiantGroupeEtudiants) {
        this.etudiantGroupeEtudiants = etudiantGroupeEtudiants;
    }

    public Convention getConvention() {
        return convention;
    }

    public void setConvention(Convention convention) {
        this.convention = convention;
    }
}
