package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "GroupeEtudiant")
public class GroupeEtudiant extends ObjetMetier implements Exportable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idGroupeEtudiant", nullable = false)
    private int id;

    @Column(name = "code", nullable = false, length = 100, unique = true)
    private String code;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @OneToMany(mappedBy = "groupeEtudiant", fetch = FetchType.EAGER, cascade = {CascadeType.MERGE}, orphanRemoval = true)
    private List<EtudiantGroupeEtudiant> etudiantGroupeEtudiants;

    @OneToMany(mappedBy = "groupeEtudiant", cascade = {CascadeType.MERGE}, orphanRemoval = true)
    private List<HistoriqueMailGroupe> historiqueMailGroupes;

    @OneToOne(cascade = {CascadeType.REMOVE}, fetch = FetchType.EAGER)
    @JoinColumn(name = "idConvention", nullable = false)
    private Convention convention;

    @Column(nullable = false)
    private boolean validationCreation = false;

    @Column(nullable = false)
    private boolean infosStageValid = false;


    public List<HistoriqueMailGroupe> getHistoriqueMailGroupe() {
        return historiqueMailGroupes;
    }

    public void setHistoriqueMailGroupe(List<HistoriqueMailGroupe> historiqueMailGroupes) {
        this.historiqueMailGroupes = historiqueMailGroupes;
    }

    @Override
    public String getExportValue(String key) {
        return null;
    }
}
