package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.view.Views;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(
        name = "Etudiant",
        uniqueConstraints = {
                @UniqueConstraint(name = "index_ident_etudiant_code_universite", columnNames = {"identEtudiant", "codeUniversite"})
        }
)
public class Etudiant extends ObjetMetier {

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEtudiant", nullable = false)
    private int id;

    @JsonView(Views.List.class)
    @Column(nullable = false, length = 50)
    private String nom;

    @JsonView(Views.List.class)
    @Column(nullable = false, length = 50)
    private String prenom;

    @Column(length = 50)
    private String mail;

    @Column(nullable = false, length = 10)
    private String codeUniversite;

    @Column(nullable = false, length = 50)
    private String identEtudiant;

    @Column(length = 50)
    private String nomMarital;

    @Column(nullable = false, length = 20)
    private String numEtudiant;

    @Column(length = 15)
    private String numSS;

    @Column(length = 1)
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

    public String getNumSS() {
        return numSS;
    }

    public void setNumSS(String numSS) {
        this.numSS = numSS;
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
}
