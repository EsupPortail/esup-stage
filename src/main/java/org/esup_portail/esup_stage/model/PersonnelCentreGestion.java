package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PersonnelCentreGestion")
public class PersonnelCentreGestion extends ObjetMetier implements Exportable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPersonnelCentreGestion", nullable = false)
    private int id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column
    private String mail;

    @Column(name = "telephone")
    private String tel;

    @Column
    private String fax;

    @ManyToOne
    @JoinColumn(name = "idCivilite")
    private Civilite civilite;

    @Column
    private String codeUniversite;

    @Column
    private String typePersonne;

    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    @JsonBackReference
    private CentreGestion centreGestion;

    @Column(nullable = false)
    private String uidPersonnel;

    @Column
    private String fonction;

    @ManyToOne
    @JoinColumn(name = "idDroitAdministration", nullable = false)
    private DroitAdministration droitAdministration;

    @Column(nullable = false)
    private boolean impressionConvention;

    @Column
    private String campus;

    @Column
    private String batiment;

    @Column
    private String bureau;

    @Column(nullable = false)
    private String codeAffectation;

    @Column(nullable = false)
    private String codeUniversiteAffectation;

    private Boolean alertesMail;
    private Boolean droitEvaluationEtudiant;
    private Boolean droitEvaluationEnseignant;
    private Boolean droitEvaluationEntreprise;

    private Boolean creationConventionEtudiant;
    private Boolean modificationConventionEtudiant;
    private Boolean creationConventionGestionnaire;
    private Boolean modificationConventionGestionnaire;
    private Boolean creationAvenantEtudiant;
    private Boolean modificationAvenantEtudiant;
    private Boolean creationAvenantGestionnaire;
    private Boolean modificationAvenantGestionnaire;
    private Boolean validationPedagogiqueConvention;
    private Boolean validationAdministrativeConvention;
    private Boolean validationAvenant;

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

    public Boolean getCreationConventionEtudiant() {
        return creationConventionEtudiant;
    }

    public void setCreationConventionEtudiant(Boolean creationConventionEtudiant) {
        this.creationConventionEtudiant = creationConventionEtudiant;
    }

    public Boolean getModificationConventionEtudiant() {
        return modificationConventionEtudiant;
    }

    public void setModificationConventionEtudiant(Boolean modificationConventionEtudiant) {
        this.modificationConventionEtudiant = modificationConventionEtudiant;
    }

    public Boolean getCreationConventionGestionnaire() {
        return creationConventionGestionnaire;
    }

    public void setCreationConventionGestionnaire(Boolean creationConventionGestionnaire) {
        this.creationConventionGestionnaire = creationConventionGestionnaire;
    }

    public Boolean getModificationConventionGestionnaire() {
        return modificationConventionGestionnaire;
    }

    public void setModificationConventionGestionnaire(Boolean modificationConventionGestionnaire) {
        this.modificationConventionGestionnaire = modificationConventionGestionnaire;
    }

    public Boolean getCreationAvenantEtudiant() {
        return creationAvenantEtudiant;
    }

    public void setCreationAvenantEtudiant(Boolean creationAvenantEtudiant) {
        this.creationAvenantEtudiant = creationAvenantEtudiant;
    }

    public Boolean getModificationAvenantEtudiant() {
        return modificationAvenantEtudiant;
    }

    public void setModificationAvenantEtudiant(Boolean modificationAvenantEtudiant) {
        this.modificationAvenantEtudiant = modificationAvenantEtudiant;
    }

    public Boolean getCreationAvenantGestionnaire() {
        return creationAvenantGestionnaire;
    }

    public void setCreationAvenantGestionnaire(Boolean creationAvenantGestionnaire) {
        this.creationAvenantGestionnaire = creationAvenantGestionnaire;
    }

    public Boolean getModificationAvenantGestionnaire() {
        return modificationAvenantGestionnaire;
    }

    public void setModificationAvenantGestionnaire(Boolean modificationAvenantGestionnaire) {
        this.modificationAvenantGestionnaire = modificationAvenantGestionnaire;
    }

    public Boolean getValidationPedagogiqueConvention() {
        return validationPedagogiqueConvention;
    }

    public void setValidationPedagogiqueConvention(Boolean validationPedagogiqueConvention) {
        this.validationPedagogiqueConvention = validationPedagogiqueConvention;
    }

    public Boolean getValidationAdministrativeConvention() {
        return validationAdministrativeConvention;
    }

    public void setValidationAdministrativeConvention(Boolean validationAdministrativeConvention) {
        this.validationAdministrativeConvention = validationAdministrativeConvention;
    }

    public Boolean getValidationAvenant() {
        return validationAvenant;
    }

    public void setValidationAvenant(Boolean validationAvenant) {
        this.validationAvenant = validationAvenant;
    }

    @Override
    public String getExportValue(String key) {
        String value = "";
        switch (key) {
            case "civilite":
                if (getCivilite() != null) {
                    value = getCivilite().getLibelle();
                }
                break;
            case "nom":
                value = getNom();
                break;
            case "prenom":
                value = getPrenom();
                break;
            case "droitAdministration":
                if (getDroitAdministration() != null) {
                    value = getDroitAdministration().getLibelle();
                }
                break;
            case "alertesMail":
                value = getAlertesMail() != null && getAlertesMail() ? "Oui" : "Non";
                break;
            case "droitsEvaluation":
                List<String> droits = new ArrayList<>();
                if (getDroitEvaluationEtudiant() != null && getDroitEvaluationEtudiant()) droits.add("Étudiant");
                if (getDroitEvaluationEnseignant() != null && getDroitEvaluationEnseignant()) droits.add("Enseignant");
                if (getDroitEvaluationEntreprise() != null && getDroitEvaluationEntreprise()) droits.add("Entreprise");
                value = String.join(", ", droits);
                break;
            default:
                break;
        }
        return value;
    }
}
