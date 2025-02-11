package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import org.esup_portail.esup_stage.dto.view.Views;

import java.util.Date;

@Entity
@Table(
        name = "Etudiant",
        uniqueConstraints = {
                @UniqueConstraint(name = "index_ident_etudiant_code_universite", columnNames = {"identEtudiant", "codeUniversite"})
        }
)
public class Etudiant extends ObjetMetier implements Exportable {

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEtudiant", nullable = false)
    private int id;

    @JsonView(Views.List.class)
    @Column(nullable = false)
    private String nom;

    @JsonView(Views.List.class)
    @Column(nullable = false)
    private String prenom;

    @Column
    private String mail;

    @Column(nullable = false)
    private String codeUniversite;

    @Column(nullable = false)
    private String identEtudiant;

    @Column
    private String nomMarital;

    @Column(nullable = false)
    private String numEtudiant;

    @Column
    private String codeSexe;

    @Temporal(TemporalType.DATE)
    private Date dateNais;

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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getCodeUniversite() {
        return codeUniversite;
    }

    public void setCodeUniversite(String codeUniversite) {
        this.codeUniversite = codeUniversite;
    }

    public String getIdentEtudiant() {
        return identEtudiant;
    }

    public void setIdentEtudiant(String identEtudiant) {
        this.identEtudiant = identEtudiant;
    }

    public String getNomMarital() {
        return nomMarital;
    }

    public void setNomMarital(String nomMarital) {
        this.nomMarital = nomMarital;
    }

    public String getNumEtudiant() {
        return numEtudiant;
    }

    public void setNumEtudiant(String numEtudiant) {
        this.numEtudiant = numEtudiant;
    }

    public String getCodeSexe() {
        return codeSexe;
    }

    public void setCodeSexe(String codeSexe) {
        this.codeSexe = codeSexe;
    }

    public Date getDateNais() {
        return dateNais;
    }

    public void setDateNais(Date dateNais) {
        this.dateNais = dateNais;
    }

    @Override
    public String getExportValue(String key) {
        return null;
    }
}
