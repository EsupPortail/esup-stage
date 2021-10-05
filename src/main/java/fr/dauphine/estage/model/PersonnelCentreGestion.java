package fr.dauphine.estage.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "PersonnelCentreGestion")
public class PersonnelCentreGestion extends ObjetMetier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPersonnelCentreGestion", nullable = false)
    private int id;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Column(length = 50)
    private String mail;

    @Column(name = "telephone", length = 50)
    private String tel;

    @Column(length = 50)
    private String fax;

    @ManyToOne
    @JoinColumn(name = "idCivilite")
    private Civilite civilite;

    @Column(length = 50)
    private String codeUniversite;

    @Column(length = 50)
    private String typePersonne;

    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    @JsonBackReference
    private CentreGestion centreGestion;

    @Column(nullable = false, length = 50)
    private String uidPersonnel;

    @Column(length = 50)
    private String fonction;

    @ManyToOne
    @JoinColumn(name = "idDroitAdministration", nullable = false)
    private DroitAdministration droitAdministration;

    @Column(nullable = false)
    private boolean impressionConvention;

    @Column(length = 50)
    private String campus;

    @Column(length = 50)
    private String batiment;

    @Column(length = 50)
    private String bureau;

    @Column(nullable = false, length = 10)
    private String codeAffectation;

    @Column(nullable = false, length = 50)
    private String codeUniversiteAffectation;

    private Boolean alertesMail;
    private Boolean droitEvaluationEtudiant;
    private Boolean droitEvaluationEnseignant;
    private Boolean droitEvaluationEntreprise;

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

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public Civilite getCivilite() {
        return civilite;
    }

    public void setCivilite(Civilite civilite) {
        this.civilite = civilite;
    }

    public String getCodeUniversite() {
        return codeUniversite;
    }

    public void setCodeUniversite(String codeUniversite) {
        this.codeUniversite = codeUniversite;
    }

    public String getTypePersonne() {
        return typePersonne;
    }

    public void setTypePersonne(String typePersonne) {
        this.typePersonne = typePersonne;
    }

    public CentreGestion getCentreGestion() {
        return centreGestion;
    }

    public void setCentreGestion(CentreGestion centreGestion) {
        this.centreGestion = centreGestion;
    }

    public String getUidPersonnel() {
        return uidPersonnel;
    }

    public void setUidPersonnel(String uidPersonnel) {
        this.uidPersonnel = uidPersonnel;
    }

    public String getFonction() {
        return fonction;
    }

    public void setFonction(String fonction) {
        this.fonction = fonction;
    }

    public DroitAdministration getDroitAdministration() {
        return droitAdministration;
    }

    public void setDroitAdministration(DroitAdministration droitAdministration) {
        this.droitAdministration = droitAdministration;
    }

    public boolean isImpressionConvention() {
        return impressionConvention;
    }

    public void setImpressionConvention(boolean impressionConvention) {
        this.impressionConvention = impressionConvention;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getBatiment() {
        return batiment;
    }

    public void setBatiment(String batiment) {
        this.batiment = batiment;
    }

    public String getBureau() {
        return bureau;
    }

    public void setBureau(String bureau) {
        this.bureau = bureau;
    }

    public String getCodeAffectation() {
        return codeAffectation;
    }

    public void setCodeAffectation(String codeAffectation) {
        this.codeAffectation = codeAffectation;
    }

    public String getCodeUniversiteAffectation() {
        return codeUniversiteAffectation;
    }

    public void setCodeUniversiteAffectation(String codeUniversiteAffectation) {
        this.codeUniversiteAffectation = codeUniversiteAffectation;
    }

    public Boolean getAlertesMail() {
        return alertesMail;
    }

    public void setAlertesMail(Boolean alertesMail) {
        this.alertesMail = alertesMail;
    }

    public Boolean getDroitEvaluationEtudiant() {
        return droitEvaluationEtudiant;
    }

    public void setDroitEvaluationEtudiant(Boolean droitEvaluationEtudiant) {
        this.droitEvaluationEtudiant = droitEvaluationEtudiant;
    }

    public Boolean getDroitEvaluationEnseignant() {
        return droitEvaluationEnseignant;
    }

    public void setDroitEvaluationEnseignant(Boolean droitEvaluationEnseignant) {
        this.droitEvaluationEnseignant = droitEvaluationEnseignant;
    }

    public Boolean getDroitEvaluationEntreprise() {
        return droitEvaluationEntreprise;
    }

    public void setDroitEvaluationEntreprise(Boolean droitEvaluationEntreprise) {
        this.droitEvaluationEntreprise = droitEvaluationEntreprise;
    }
}
