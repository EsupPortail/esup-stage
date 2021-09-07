package fr.dauphine.estage.model;

import javax.persistence.*;

@Entity
@Table(name = "CentreGestion")
public class CentreGestion extends ObjetMetier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCentreGestion", nullable = false)
    private int id;

    @Column(length = 100)
    private String nomCentre;

    @ManyToOne
    @JoinColumn(name = "idNiveauCentre", nullable = false)
    private NiveauCentre niveauCentre;

    @Column(length = 50)
    private String siteWeb;

    @Column(length = 50)
    private String mail;

    @Column(length = 20)
    private String telephone;

    @Column(length = 20)
    private String fax;

    @Lob
    private String commentaire;

    @Column(nullable = false)
    private boolean presenceTuteurEns;

    @Column(nullable = false)
    private boolean presenceTuteurPro;

    @Column(nullable = false)
    private boolean saisieTuteurProParEtudiant;

    @Column(nullable = false)
    private boolean depotAnonyme;

    @Column(nullable = false, length = 50)
    private String codeUniversite;

    @Column(length = 50)
    private String nomViseur;

    @Column(length = 50)
    private String prenomViseur;

    @Column(length = 100)
    private String qualiteViseur;

    @Column(length = 200)
    private String urlPageInstruction;

    @ManyToOne
    @JoinColumn(name = "idCentreGestionSuperViseur")
    private CentreGestionSuperviseur centreGestionSuperViseur;

    @ManyToOne
    @JoinColumn(name = "codeConfidentialite", nullable = false)
    private Confidentialite codeConfidentialite;

    @Column(nullable = false)
    private boolean autoriserImpressionConvention;

    @ManyToOne
    @JoinColumn(name = "idFichier")
    private Fichier fichier;

    private Boolean validationPedagogique;

    @Column(nullable = false)
    private boolean autorisationEtudiantCreationConvention;

    @ManyToOne
    @JoinColumn(name = "idModeValidationStage", nullable = false)
    private ModeValidationStage modeValidationStage;

    private Boolean visibiliteEvalPro;

    private Boolean recupInscriptionAnterieure;

    private Integer dureeRecupInscriptionAnterieure;

    @Column(length = 200)
    private String adresse;

    @Column(length = 200)
    private String voie;

    @Column(length = 200)
    private String commune;

    @Column(length = 10)
    private String codePostal;

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
}
