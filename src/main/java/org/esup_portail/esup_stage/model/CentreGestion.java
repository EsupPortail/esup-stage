package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import org.esup_portail.esup_stage.dto.view.Views;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CentreGestion")
public class CentreGestion extends ObjetMetier implements Exportable {

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCentreGestion", nullable = false)
    private int id;

    @JsonView(Views.List.class)
    @Column
    private String nomCentre;

    @ManyToOne
    @JoinColumn(name = "idNiveauCentre", nullable = false)
    private NiveauCentre niveauCentre;

    @Column
    private String siteWeb;

    @Column
    private String mail;

    @Column
    private String telephone;

    @Column
    private String fax;

    @Lob
    private String commentaire;

    @Column()
    private boolean presenceTuteurEns;

    @Column()
    private boolean presenceTuteurPro;

    @Column()
    private boolean saisieTuteurProParEtudiant;

    @Column
    private boolean depotAnonyme;

    @Column(nullable = false)
    private String codeUniversite;

    @Column
    private String nomViseur;

    @Column
    private String prenomViseur;

    @Column
    private String qualiteViseur;

    @Column
    private String mailViseur;

    @Column
    private String urlPageInstruction;

    @ManyToOne
    @JoinColumn(name = "idCentreGestionSuperViseur")
    private CentreGestionSuperviseur centreGestionSuperViseur;

    @ManyToOne
    @JoinColumn(name = "codeConfidentialite")
    private Confidentialite codeConfidentialite;

    @Column()
    private boolean autoriserImpressionConvention;

    @Column()
    private Integer conditionValidationImpression = 0;

    @ManyToOne
    @JoinColumn(name = "idFichier")
    private Fichier fichier;

    @JsonView(Views.List.class)
    @Column()
    private Boolean validationPedagogique = false;

    @JsonView(Views.List.class)
    @Column()
    private Boolean validationConvention = false;

    @JsonView(Views.List.class)
    @Column()
    private Boolean verificationAdministrative = false;

    @JsonView(Views.List.class)
    private Integer validationPedagogiqueOrdre;

    @JsonView(Views.List.class)
    private Integer validationConventionOrdre;

    @JsonView(Views.List.class)
    private Integer verificationAdministrativeOrdre;

    @Column()
    private boolean autorisationEtudiantCreationConvention;

    @ManyToOne
    @JoinColumn(name = "idModeValidationStage")
    private ModeValidationStage modeValidationStage;

    @Column()
    private Boolean visibiliteEvalPro = false;

    @Column()
    private Boolean recupInscriptionAnterieure = false;

    private Integer dureeRecupInscriptionAnterieure = 0;

    @Column
    private String adresse;

    @Column
    private String voie;

    @Column
    private String commune;

    @Column
    private String codePostal;

    @OneToMany(mappedBy = "centreGestion", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private List<PersonnelCentreGestion> personnels = new ArrayList<>();

    @Column()
    private boolean validationCreation = false;

    @JsonIgnore
    @OneToMany(mappedBy = "centreGestion", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CritereGestion> criteres = new ArrayList<>();

    @Column()
    private Integer delaiAlerteConvention = 0;

    @OneToOne(mappedBy = "centreGestion", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Consigne consigne;

    @JsonView(Views.List.class)
    @OneToOne(mappedBy = "centreGestion", cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private FicheEvaluation ficheEvaluation;

    @Column(length = 255)
    private String circuitSignature;

    @OneToMany(mappedBy = "centreGestion", cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.PERSIST})
    @OrderBy("ordre ASC")
    @JsonManagedReference
    private List<CentreGestionSignataire> signataires = new ArrayList<>();

    @JsonView(Views.List.class)
    @Column()
    private Boolean onlyMailCentreGestion = false;

    @Column
    private boolean envoiDocumentSigne = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomCentre() {
        return nomCentre;
    }

    public void setNomCentre(String nomCentre) {
        this.nomCentre = nomCentre;
    }

    public NiveauCentre getNiveauCentre() {
        return niveauCentre;
    }

    public void setNiveauCentre(NiveauCentre niveauCentre) {
        this.niveauCentre = niveauCentre;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public boolean isPresenceTuteurEns() {
        return presenceTuteurEns;
    }

    public void setPresenceTuteurEns(boolean presenceTuteurEns) {
        this.presenceTuteurEns = presenceTuteurEns;
    }

    public boolean isPresenceTuteurPro() {
        return presenceTuteurPro;
    }

    public void setPresenceTuteurPro(boolean presenceTuteurPro) {
        this.presenceTuteurPro = presenceTuteurPro;
    }

    public boolean isSaisieTuteurProParEtudiant() {
        return saisieTuteurProParEtudiant;
    }

    public void setSaisieTuteurProParEtudiant(boolean saisieTuteurProParEtudiant) {
        this.saisieTuteurProParEtudiant = saisieTuteurProParEtudiant;
    }

    public boolean isDepotAnonyme() {
        return depotAnonyme;
    }

    public void setDepotAnonyme(boolean depotAnonyme) {
        this.depotAnonyme = depotAnonyme;
    }

    public String getCodeUniversite() {
        return codeUniversite;
    }

    public void setCodeUniversite(String codeUniversite) {
        this.codeUniversite = codeUniversite;
    }

    public String getNomViseur() {
        return nomViseur;
    }

    public void setNomViseur(String nomViseur) {
        this.nomViseur = nomViseur;
    }

    public String getPrenomViseur() {
        return prenomViseur;
    }

    public void setPrenomViseur(String prenomViseur) {
        this.prenomViseur = prenomViseur;
    }

    public String getQualiteViseur() {
        return qualiteViseur;
    }

    public void setQualiteViseur(String qualiteViseur) {
        this.qualiteViseur = qualiteViseur;
    }

    public String getMailViseur() {
        return mailViseur;
    }

    public void setMailViseur(String mailViseur) {
        this.mailViseur = mailViseur;
    }

    public String getUrlPageInstruction() {
        return urlPageInstruction;
    }

    public void setUrlPageInstruction(String urlPageInstruction) {
        this.urlPageInstruction = urlPageInstruction;
    }

    public CentreGestionSuperviseur getCentreGestionSuperViseur() {
        return centreGestionSuperViseur;
    }

    public void setCentreGestionSuperViseur(CentreGestionSuperviseur centreGestionSuperViseur) {
        this.centreGestionSuperViseur = centreGestionSuperViseur;
    }

    public Confidentialite getCodeConfidentialite() {
        return codeConfidentialite;
    }

    public void setCodeConfidentialite(Confidentialite codeConfidentialite) {
        this.codeConfidentialite = codeConfidentialite;
    }

    public boolean isAutoriserImpressionConvention() {
        return autoriserImpressionConvention;
    }

    public void setAutoriserImpressionConvention(boolean autoriserImpressionConvention) {
        this.autoriserImpressionConvention = autoriserImpressionConvention;
    }

    public Integer getConditionValidationImpression() {
        return conditionValidationImpression;
    }

    public void setConditionValidationImpression(Integer conditionValidationImpression) {
        this.conditionValidationImpression = conditionValidationImpression;
    }

    public Fichier getFichier() {
        return fichier;
    }

    public void setFichier(Fichier fichier) {
        this.fichier = fichier;
    }

    public Boolean getValidationPedagogique() {
        return validationPedagogique;
    }

    public void setValidationPedagogique(Boolean validationPedagogique) {
        this.validationPedagogique = validationPedagogique;
    }

    public Boolean getValidationConvention() {
        return validationConvention;
    }

    public void setValidationConvention(Boolean validationConvention) {
        this.validationConvention = validationConvention;
    }

    public Boolean getVerificationAdministrative() {
        return verificationAdministrative;
    }

    public void setVerificationAdministrative(Boolean verificationAdministrative) {
        this.verificationAdministrative = verificationAdministrative;
    }

    public Integer getValidationPedagogiqueOrdre() {
        return validationPedagogiqueOrdre;
    }

    public void setValidationPedagogiqueOrdre(Integer validationPedagogiqueOrdre) {
        this.validationPedagogiqueOrdre = validationPedagogiqueOrdre;
    }

    public Integer getValidationConventionOrdre() {
        return validationConventionOrdre;
    }

    public void setValidationConventionOrdre(Integer validationConventionOrdre) {
        this.validationConventionOrdre = validationConventionOrdre;
    }

    public Integer getVerificationAdministrativeOrdre() {
        return verificationAdministrativeOrdre;
    }

    public void setVerificationAdministrativeOrdre(Integer verificationAdministrativeOrdre) {
        this.verificationAdministrativeOrdre = verificationAdministrativeOrdre;
    }

    public boolean isAutorisationEtudiantCreationConvention() {
        return autorisationEtudiantCreationConvention;
    }

    public void setAutorisationEtudiantCreationConvention(boolean autorisationEtudiantCreationConvention) {
        this.autorisationEtudiantCreationConvention = autorisationEtudiantCreationConvention;
    }

    public ModeValidationStage getModeValidationStage() {
        return modeValidationStage;
    }

    public void setModeValidationStage(ModeValidationStage modeValidationStage) {
        this.modeValidationStage = modeValidationStage;
    }

    public Boolean getVisibiliteEvalPro() {
        return visibiliteEvalPro;
    }

    public void setVisibiliteEvalPro(Boolean visibiliteEvalPro) {
        this.visibiliteEvalPro = visibiliteEvalPro;
    }

    public Boolean getRecupInscriptionAnterieure() {
        return recupInscriptionAnterieure;
    }

    public void setRecupInscriptionAnterieure(Boolean recupInscriptionAnterieure) {
        this.recupInscriptionAnterieure = recupInscriptionAnterieure;
    }

    public Integer getDureeRecupInscriptionAnterieure() {
        return dureeRecupInscriptionAnterieure;
    }

    public void setDureeRecupInscriptionAnterieure(Integer dureeRecupInscriptionAnterieure) {
        this.dureeRecupInscriptionAnterieure = dureeRecupInscriptionAnterieure;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVoie() {
        return voie;
    }

    public void setVoie(String voie) {
        this.voie = voie;
    }

    public String getCommune() {
        return commune;
    }

    public void setCommune(String commune) {
        this.commune = commune;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public List<PersonnelCentreGestion> getPersonnels() {
        return personnels;
    }

    public void setPersonnels(List<PersonnelCentreGestion> personnels) {
        this.personnels = personnels;
    }

    public boolean isValidationCreation() {
        return validationCreation;
    }

    public void setValidationCreation(boolean validationCreation) {
        this.validationCreation = validationCreation;
    }

    public List<CritereGestion> getCriteres() {
        return criteres;
    }

    public void setCriteres(List<CritereGestion> criteres) {
        this.criteres = criteres;
    }

    public Integer getDelaiAlerteConvention() {
        return delaiAlerteConvention;
    }

    public void setDelaiAlerteConvention(Integer delaiAlerteConvention) {
        this.delaiAlerteConvention = delaiAlerteConvention;
    }

    public Consigne getConsigne() {
        return consigne;
    }

    public void setConsigne(Consigne consigne) {
        this.consigne = consigne;
    }

    public FicheEvaluation getFicheEvaluation() {
        return ficheEvaluation;
    }

    public void setFicheEvaluation(FicheEvaluation ficheEvaluation) {
        this.ficheEvaluation = ficheEvaluation;
    }

    public String getCircuitSignature() {
        return circuitSignature;
    }

    public void setCircuitSignature(String circuitSignature) {
        this.circuitSignature = circuitSignature;
    }

    public List<CentreGestionSignataire> getSignataires() {
        return signataires;
    }

    public void setSignataires(List<CentreGestionSignataire> signataires) {
        this.signataires = signataires;
    }

    @Transient
    public String getAdresseComplete() {
        return getVoie() + " " + getCodePostal() + " " + getCommune();
    }

    public boolean isOnlyMailCentreGestion() {
        return onlyMailCentreGestion;
    }

    public void setOnlyMailCentreGestion(boolean onlyMailCentreGestion) {
        this.onlyMailCentreGestion = onlyMailCentreGestion;
    }

    public boolean isEnvoiDocumentSigne() {
        return envoiDocumentSigne;
    }

    public void setEnvoiDocumentSigne(boolean envoiDocumentSigne) {
        this.envoiDocumentSigne = envoiDocumentSigne;
    }

    @Override
    public String getExportValue(String key) {
        String value = "";
        switch (key) {
            case "id":
                value = String.valueOf(getId());
                break;
            case "nomCentre":
                value = getNomCentre();
                break;
            case "niveauCentre":
                if (getNiveauCentre() != null) {
                    value = getNiveauCentre().getLibelle();
                }
                break;
            case "validationPedagogique":
                value = getValidationPedagogique() != null && getValidationPedagogique() ? "Oui" : "Non";
                break;
            case "codeConfidentialite":
                if (getCodeConfidentialite() != null) {
                    value = getCodeConfidentialite().getLibelle();
                }
                break;
            default:
                break;
        }
        return value;
    }
}
