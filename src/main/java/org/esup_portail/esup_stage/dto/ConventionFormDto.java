package org.esup_portail.esup_stage.dto;

import org.esup_portail.esup_stage.enums.NbJoursHebdoEnum;
import org.esup_portail.esup_stage.model.Pays;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

public class ConventionFormDto {

    @NotNull
    @NotEmpty
    private String etudiantLogin;

    @NotNull
    @NotEmpty
    private String numEtudiant;

    @NotNull
    @NotEmpty
    private String codeEtape;

    @NotNull
    @NotEmpty
    private String codeVerionEtape;

    @NotNull
    @NotEmpty
    private String codeComposante;

    @Size(max = 10)
    private String codeDepartement;

    private int idEnseignant;

    private int idStructure;

    private int idService;

    private int idContact;

    private int idSignataire;

    @NotNull
    private int idTypeConvention;

    private int idOffre;

    private String sujetStage;

    private Date dateDebutStage;

    private Date dateFinStage;

    private Boolean interruptionStage;

    private Date dateDebutInterruption;

    private Date dateFinInterruption;

    private NbJoursHebdoEnum nbJoursHebdo;

    private int idTempsTravail;

    private String commentaireDureeTravail;

    @NotNull
    @NotEmpty
    private String codeLangueConvention;

    private int idOrigineStage;

    private int idTheme;

    private Boolean conventionStructure;

    private Boolean validationPedagogique;

    private Boolean validationConvention;

    private Boolean conversionEnContrat;

    private String commentaireStage;

    @Size(max = 200)
    private String adresseEtudiant;

    @Size(max = 10)
    private String codePostalEtudiant;

    @Size(max = 80)
    private String villeEtudiant;

    @Size(max = 50)
    private String paysEtudiant;

    @Email
    @Size(max = 100)
    private String courrielPersoEtudiant;

    @Size(max = 20)
    private String telEtudiant;

    @Size(max = 20)
    private String telPortableEtudiant;

    private int idIndemnisation;

    @Size(max = 15)
    private String montantGratification;

    private String fonctionsEtTaches;

    private String details;

    @NotNull
    @NotEmpty
    @Size(max = 10)
    private String annee;

    private int idAssurance;

    @Size(max = 15)
    private String insee;

    @Size(max = 5)
    private String codeCaisse;

    @Size(max = 5)
    private String nbHeuresHebdo;

    private Integer quotiteTravail;

    private String modeEncadreSuivi;

    private int idModeVersGratification;

    private String avantagesNature;

    private int idNatureTravail;

    private int idModeValidationStage;

    @Size(max = 8)
    private String codeElp;

    @Size(max = 60)
    private String libelleELP;

    private BigDecimal creditECTS;

    private String travailNuitFerie;

    private Integer dureeStage;

    @Size(max = 100)
    private String nomEtabRef;

    @Size(max = 200)
    private String adresseEtabRef;

    @Size(max = 30)
    private String nomSignataireComposante;

    @Size(max = 60)
    private String qualiteSignataire;

    @Size(max = 100)
    private String libelleCPAM;

    @Size(max = 4)
    private String dureeExceptionnelle;

    private int idUniteDureeExceptionnelle;

    private int idUniteGratification;

    @Size(max = 3)
    private String codeFinalite;

    @Size(max = 60)
    private String libelleFinalite;

    @Size(max = 1)
    private String codeCursusLMD;

    private Boolean priseEnChargeFraisMission;

    @Size(max = 1)
    private String codeRGI;

    @Size(max = 50)
    private String loginValidation;

    private Date dateValidation;

    @Size(max = 50)
    private String loginSignature;

    private Date dateSignature;

    private Boolean envoiMailEtudiant;

    private Date dateEnvoiMailEtudiant;

    private Boolean envoiMailTuteurPedago;

    private Date dateEnvoiMailTuteurPedago;

    private Boolean envoiMailTuteurPro;

    private Date dateEnvoiMailTuteurPro;

    private String nbConges;
    private String competences;

    private int idUniteDureeGratification;

    @Size(max = 50)
    private String monnaieGratification;

    @Size(max = 10)
    private String volumeHoraireFormation;

    @Size(max = 30)
    private String typePresence;

    private int idDevise;

    private Pays paysConvention;

    private boolean horairesReguliers = false;


    public String getEtudiantLogin() {
        return etudiantLogin;
    }

    public void setEtudiantLogin(String etudiantLogin) {
        this.etudiantLogin = etudiantLogin;
    }

    public String getNumEtudiant() {
        return numEtudiant;
    }

    public void setNumEtudiant(String numEtudiant) {
        this.numEtudiant = numEtudiant;
    }

    public String getCodeEtape() {
        return codeEtape;
    }

    public void setCodeEtape(String codeEtape) {
        this.codeEtape = codeEtape;
    }

    public String getCodeVerionEtape() {
        return codeVerionEtape;
    }

    public void setCodeVerionEtape(String codeVerionEtape) {
        this.codeVerionEtape = codeVerionEtape;
    }

    public String getCodeComposante() {
        return codeComposante;
    }

    public void setCodeComposante(String codeComposante) {
        this.codeComposante = codeComposante;
    }

    public String getCodeDepartement() {
        return codeDepartement;
    }

    public void setCodeDepartement(String codeDepartement) {
        this.codeDepartement = codeDepartement;
    }

    public int getIdEnseignant() {
        return idEnseignant;
    }

    public void setIdEnseignant(int idEnseignant) {
        this.idEnseignant = idEnseignant;
    }

    public int getIdStructure() {
        return idStructure;
    }

    public void setIdStructure(int idStructure) {
        this.idStructure = idStructure;
    }

    public int getIdService() {
        return idService;
    }

    public void setIdService(int idService) {
        this.idService = idService;
    }

    public int getIdContact() {
        return idContact;
    }

    public void setIdContact(int idContact) {
        this.idContact = idContact;
    }

    public int getIdSignataire() {
        return idSignataire;
    }

    public void setIdSignataire(int idSignataire) {
        this.idSignataire = idSignataire;
    }

    public int getIdTypeConvention() {
        return idTypeConvention;
    }

    public void setIdTypeConvention(int idTypeConvention) {
        this.idTypeConvention = idTypeConvention;
    }

    public int getIdOffre() {
        return idOffre;
    }

    public void setIdOffre(int idOffre) {
        this.idOffre = idOffre;
    }

    public String getSujetStage() {
        return sujetStage;
    }

    public void setSujetStage(String sujetStage) {
        this.sujetStage = sujetStage;
    }

    public Date getDateDebutStage() {
        return dateDebutStage;
    }

    public void setDateDebutStage(Date dateDebutStage) {
        this.dateDebutStage = dateDebutStage;
    }

    public Date getDateFinStage() {
        return dateFinStage;
    }

    public void setDateFinStage(Date dateFinStage) {
        this.dateFinStage = dateFinStage;
    }

    public Boolean getInterruptionStage() {
        return interruptionStage;
    }

    public void setInterruptionStage(Boolean interruptionStage) {
        this.interruptionStage = interruptionStage;
    }

    public Date getDateDebutInterruption() {
        return dateDebutInterruption;
    }

    public void setDateDebutInterruption(Date dateDebutInterruption) {
        this.dateDebutInterruption = dateDebutInterruption;
    }

    public Date getDateFinInterruption() {
        return dateFinInterruption;
    }

    public void setDateFinInterruption(Date dateFinInterruption) {
        this.dateFinInterruption = dateFinInterruption;
    }

    public NbJoursHebdoEnum getNbJoursHebdo() {
        return nbJoursHebdo;
    }

    public void setNbJoursHebdo(NbJoursHebdoEnum nbJoursHebdo) {
        this.nbJoursHebdo = nbJoursHebdo;
    }

    public int getIdTempsTravail() {
        return idTempsTravail;
    }

    public void setIdTempsTravail(int idTempsTravail) {
        this.idTempsTravail = idTempsTravail;
    }

    public String getCommentaireDureeTravail() {
        return commentaireDureeTravail;
    }

    public void setCommentaireDureeTravail(String commentaireDureeTravail) {
        this.commentaireDureeTravail = commentaireDureeTravail;
    }

    public String getCodeLangueConvention() {
        return codeLangueConvention;
    }

    public void setCodeLangueConvention(String codeLangueConvention) {
        this.codeLangueConvention = codeLangueConvention;
    }

    public int getIdOrigineStage() {
        return idOrigineStage;
    }

    public void setIdOrigineStage(int idOrigineStage) {
        this.idOrigineStage = idOrigineStage;
    }

    public int getIdTheme() {
        return idTheme;
    }

    public void setIdTheme(int idTheme) {
        this.idTheme = idTheme;
    }

    public Boolean getConventionStructure() {
        return conventionStructure;
    }

    public void setConventionStructure(Boolean conventionStructure) {
        this.conventionStructure = conventionStructure;
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

    public Boolean getConversionEnContrat() {
        return conversionEnContrat;
    }

    public void setConversionEnContrat(Boolean conversionEnContrat) {
        this.conversionEnContrat = conversionEnContrat;
    }

    public String getCommentaireStage() {
        return commentaireStage;
    }

    public void setCommentaireStage(String commentaireStage) {
        this.commentaireStage = commentaireStage;
    }

    public String getAdresseEtudiant() {
        return adresseEtudiant;
    }

    public void setAdresseEtudiant(String adresseEtudiant) {
        this.adresseEtudiant = adresseEtudiant;
    }

    public String getCodePostalEtudiant() {
        return codePostalEtudiant;
    }

    public void setCodePostalEtudiant(String codePostalEtudiant) {
        this.codePostalEtudiant = codePostalEtudiant;
    }

    public String getVilleEtudiant() {
        return villeEtudiant;
    }

    public void setVilleEtudiant(String villeEtudiant) {
        this.villeEtudiant = villeEtudiant;
    }

    public String getPaysEtudiant() {
        return paysEtudiant;
    }

    public void setPaysEtudiant(String paysEtudiant) {
        this.paysEtudiant = paysEtudiant;
    }

    public String getCourrielPersoEtudiant() {
        return courrielPersoEtudiant;
    }

    public void setCourrielPersoEtudiant(String courrielPersoEtudiant) {
        this.courrielPersoEtudiant = courrielPersoEtudiant;
    }

    public String getTelEtudiant() {
        return telEtudiant;
    }

    public void setTelEtudiant(String telEtudiant) {
        this.telEtudiant = telEtudiant;
    }

    public String getTelPortableEtudiant() {
        return telPortableEtudiant;
    }

    public void setTelPortableEtudiant(String telPortableEtudiant) {
        this.telPortableEtudiant = telPortableEtudiant;
    }

    public int getIdIndemnisation() {
        return idIndemnisation;
    }

    public void setIdIndemnisation(int idIndemnisation) {
        this.idIndemnisation = idIndemnisation;
    }

    public String getMontantGratification() {
        return montantGratification;
    }

    public void setMontantGratification(String montantGratification) {
        this.montantGratification = montantGratification;
    }

    public String getFonctionsEtTaches() {
        return fonctionsEtTaches;
    }

    public void setFonctionsEtTaches(String fonctionsEtTaches) {
        this.fonctionsEtTaches = fonctionsEtTaches;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getAnnee() {
        return annee;
    }

    public void setAnnee(String annee) {
        this.annee = annee;
    }

    public int getIdAssurance() {
        return idAssurance;
    }

    public void setIdAssurance(int idAssurance) {
        this.idAssurance = idAssurance;
    }

    public String getInsee() {
        return insee;
    }

    public void setInsee(String insee) {
        this.insee = insee;
    }

    public String getCodeCaisse() {
        return codeCaisse;
    }

    public void setCodeCaisse(String codeCaisse) {
        this.codeCaisse = codeCaisse;
    }

    public String getNbHeuresHebdo() {
        return nbHeuresHebdo;
    }

    public void setNbHeuresHebdo(String nbHeuresHebdo) {
        this.nbHeuresHebdo = nbHeuresHebdo;
    }

    public Integer getQuotiteTravail() {
        return quotiteTravail;
    }

    public void setQuotiteTravail(Integer quotiteTravail) {
        this.quotiteTravail = quotiteTravail;
    }

    public String getModeEncadreSuivi() {
        return modeEncadreSuivi;
    }

    public void setModeEncadreSuivi(String modeEncadreSuivi) {
        this.modeEncadreSuivi = modeEncadreSuivi;
    }

    public int getIdModeVersGratification() {
        return idModeVersGratification;
    }

    public void setIdModeVersGratification(int idModeVersGratification) {
        this.idModeVersGratification = idModeVersGratification;
    }

    public String getAvantagesNature() {
        return avantagesNature;
    }

    public void setAvantagesNature(String avantagesNature) {
        this.avantagesNature = avantagesNature;
    }

    public int getIdNatureTravail() {
        return idNatureTravail;
    }

    public void setIdNatureTravail(int idNatureTravail) {
        this.idNatureTravail = idNatureTravail;
    }

    public int getIdModeValidationStage() {
        return idModeValidationStage;
    }

    public void setIdModeValidationStage(int idModeValidationStage) {
        this.idModeValidationStage = idModeValidationStage;
    }

    public String getCodeElp() {
        return codeElp;
    }

    public void setCodeElp(String codeElp) {
        this.codeElp = codeElp;
    }

    public String getLibelleELP() {
        return libelleELP;
    }

    public void setLibelleELP(String libelleELP) {
        this.libelleELP = libelleELP;
    }

    public BigDecimal getCreditECTS() {
        return creditECTS;
    }

    public void setCreditECTS(BigDecimal creditECTS) {
        this.creditECTS = creditECTS;
    }

    public String getTravailNuitFerie() {
        return travailNuitFerie;
    }

    public void setTravailNuitFerie(String travailNuitFerie) {
        this.travailNuitFerie = travailNuitFerie;
    }

    public Integer getDureeStage() {
        return dureeStage;
    }

    public void setDureeStage(Integer dureeStage) {
        this.dureeStage = dureeStage;
    }

    public String getNomEtabRef() {
        return nomEtabRef;
    }

    public void setNomEtabRef(String nomEtabRef) {
        this.nomEtabRef = nomEtabRef;
    }

    public String getAdresseEtabRef() {
        return adresseEtabRef;
    }

    public void setAdresseEtabRef(String adresseEtabRef) {
        this.adresseEtabRef = adresseEtabRef;
    }

    public String getNomSignataireComposante() {
        return nomSignataireComposante;
    }

    public void setNomSignataireComposante(String nomSignataireComposante) {
        this.nomSignataireComposante = nomSignataireComposante;
    }

    public String getQualiteSignataire() {
        return qualiteSignataire;
    }

    public void setQualiteSignataire(String qualiteSignataire) {
        this.qualiteSignataire = qualiteSignataire;
    }

    public String getLibelleCPAM() {
        return libelleCPAM;
    }

    public void setLibelleCPAM(String libelleCPAM) {
        this.libelleCPAM = libelleCPAM;
    }

    public String getDureeExceptionnelle() {
        return dureeExceptionnelle;
    }

    public void setDureeExceptionnelle(String dureeExceptionnelle) {
        this.dureeExceptionnelle = dureeExceptionnelle;
    }

    public int getIdUniteDureeExceptionnelle() {
        return idUniteDureeExceptionnelle;
    }

    public void setIdUniteDureeExceptionnelle(int idUniteDureeExceptionnelle) {
        this.idUniteDureeExceptionnelle = idUniteDureeExceptionnelle;
    }

    public int getIdUniteGratification() {
        return idUniteGratification;
    }

    public void setIdUniteGratification(int idUniteGratification) {
        this.idUniteGratification = idUniteGratification;
    }

    public String getCodeFinalite() {
        return codeFinalite;
    }

    public void setCodeFinalite(String codeFinalite) {
        this.codeFinalite = codeFinalite;
    }

    public String getLibelleFinalite() {
        return libelleFinalite;
    }

    public void setLibelleFinalite(String libelleFinalite) {
        this.libelleFinalite = libelleFinalite;
    }

    public String getCodeCursusLMD() {
        return codeCursusLMD;
    }

    public void setCodeCursusLMD(String codeCursusLMD) {
        this.codeCursusLMD = codeCursusLMD;
    }

    public Boolean getPriseEnChargeFraisMission() {
        return priseEnChargeFraisMission;
    }

    public void setPriseEnChargeFraisMission(Boolean priseEnChargeFraisMission) {
        this.priseEnChargeFraisMission = priseEnChargeFraisMission;
    }

    public String getCodeRGI() {
        return codeRGI;
    }

    public void setCodeRGI(String codeRGI) {
        this.codeRGI = codeRGI;
    }

    public String getLoginValidation() {
        return loginValidation;
    }

    public void setLoginValidation(String loginValidation) {
        this.loginValidation = loginValidation;
    }

    public Date getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(Date dateValidation) {
        this.dateValidation = dateValidation;
    }

    public String getLoginSignature() {
        return loginSignature;
    }

    public void setLoginSignature(String loginSignature) {
        this.loginSignature = loginSignature;
    }

    public Date getDateSignature() {
        return dateSignature;
    }

    public void setDateSignature(Date dateSignature) {
        this.dateSignature = dateSignature;
    }

    public Boolean getEnvoiMailEtudiant() {
        return envoiMailEtudiant;
    }

    public void setEnvoiMailEtudiant(Boolean envoiMailEtudiant) {
        this.envoiMailEtudiant = envoiMailEtudiant;
    }

    public Date getDateEnvoiMailEtudiant() {
        return dateEnvoiMailEtudiant;
    }

    public void setDateEnvoiMailEtudiant(Date dateEnvoiMailEtudiant) {
        this.dateEnvoiMailEtudiant = dateEnvoiMailEtudiant;
    }

    public Boolean getEnvoiMailTuteurPedago() {
        return envoiMailTuteurPedago;
    }

    public void setEnvoiMailTuteurPedago(Boolean envoiMailTuteurPedago) {
        this.envoiMailTuteurPedago = envoiMailTuteurPedago;
    }

    public Date getDateEnvoiMailTuteurPedago() {
        return dateEnvoiMailTuteurPedago;
    }

    public void setDateEnvoiMailTuteurPedago(Date dateEnvoiMailTuteurPedago) {
        this.dateEnvoiMailTuteurPedago = dateEnvoiMailTuteurPedago;
    }

    public Boolean getEnvoiMailTuteurPro() {
        return envoiMailTuteurPro;
    }

    public void setEnvoiMailTuteurPro(Boolean envoiMailTuteurPro) {
        this.envoiMailTuteurPro = envoiMailTuteurPro;
    }

    public Date getDateEnvoiMailTuteurPro() {
        return dateEnvoiMailTuteurPro;
    }

    public void setDateEnvoiMailTuteurPro(Date dateEnvoiMailTuteurPro) {
        this.dateEnvoiMailTuteurPro = dateEnvoiMailTuteurPro;
    }

    public String getNbConges() {
        return nbConges;
    }

    public void setNbConges(String nbConges) {
        this.nbConges = nbConges;
    }

    public String getCompetences() {
        return competences;
    }

    public void setCompetences(String competences) {
        this.competences = competences;
    }

    public int getIdUniteDureeGratification() {
        return idUniteDureeGratification;
    }

    public void setIdUniteDureeGratification(int idUniteDureeGratification) {
        this.idUniteDureeGratification = idUniteDureeGratification;
    }

    public String getMonnaieGratification() {
        return monnaieGratification;
    }

    public void setMonnaieGratification(String monnaieGratification) {
        this.monnaieGratification = monnaieGratification;
    }

    public String getVolumeHoraireFormation() {
        return volumeHoraireFormation;
    }

    public void setVolumeHoraireFormation(String volumeHoraireFormation) {
        this.volumeHoraireFormation = volumeHoraireFormation;
    }

    public String getTypePresence() {
        return typePresence;
    }

    public void setTypePresence(String typePresence) {
        this.typePresence = typePresence;
    }

    public int getIdDevise() {
        return idDevise;
    }

    public void setIdDevise(int idDevise) {
        this.idDevise = idDevise;
    }

    public Pays getPaysConvention() {
        return paysConvention;
    }

    public void setPaysConvention(Pays paysConvention) {
        this.paysConvention = paysConvention;
    }

    public boolean isHorairesReguliers() {
        return horairesReguliers;
    }

    public void setHorairesReguliers(boolean horairesReguliers) {
        this.horairesReguliers = horairesReguliers;
    }
}
