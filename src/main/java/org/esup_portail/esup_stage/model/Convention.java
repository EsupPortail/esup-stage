package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.NbJoursHebdoEnum;
import org.esup_portail.esup_stage.service.PeriodeService;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Entity
@Table(name = "Convention")
public class Convention extends ObjetMetier implements Exportable {

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idConvention", nullable = false)
    private int id;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumn(name = "idEtudiant", nullable = false)
    private Etudiant etudiant;

    @JsonView(Views.List.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idCentreGestion")
    private CentreGestion centreGestion;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "codeUFR", referencedColumnName = "codeUFR"),
            @JoinColumn(name = "codeUniversiteUFR", referencedColumnName = "codeUniversite"),
    })
    private Ufr ufr;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "codeEtape", referencedColumnName = "codeEtape"),
            @JoinColumn(name = "codeVersionEtape", referencedColumnName = "codeVersionEtape"),
            @JoinColumn(name = "codeUniversiteEtape", referencedColumnName = "codeUniversite"),
    })
    private Etape etape;

    @Column
    private String codeDepartement;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumn(name = "idEnseignant")
    private Enseignant enseignant;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumn(name = "idStructure")
    private Structure structure;

    @ManyToOne
    @JoinColumn(name = "idService")
    private Service service;

    @ManyToOne
    @JoinColumn(name = "idContact")
    private Contact contact;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumn(name = "idSignataire")
    private Contact signataire;

    @ManyToOne
    @JoinColumn(name = "idTypeConvention")
    private TypeConvention typeConvention;

    @ManyToOne
    @JoinColumn(name = "idOffre")
    private Offre offre;

    @JsonView(Views.List.class)
    @Column()
    private String sujetStage;

    @JsonView(Views.List.class)
    @Column()
    @Temporal(TemporalType.DATE)
    private Date dateDebutStage;

    @JsonView(Views.List.class)
    @Column()
    @Temporal(TemporalType.DATE)
    private Date dateFinStage;

    @Column()
    private Boolean interruptionStage;

    @Column()
    private NbJoursHebdoEnum nbJoursHebdo;

    @ManyToOne
    @JoinColumn(name = "idTempsTravail")
    private TempsTravail tempsTravail;

    private String commentaireDureeTravail;

    @ManyToOne
    @JoinColumn(name = "codeLangueConvention")
    private LangueConvention langueConvention;

    @ManyToOne
    @JoinColumn(name = "idOrigineStage")
    private OrigineStage origineStage;

    @ManyToOne
    @JoinColumn(name = "idTheme")
    private Theme theme;

    private Boolean conventionStructure;

    @JsonView(Views.List.class)
    private Boolean validationPedagogique = false;

    @JsonView(Views.List.class)
    private Boolean validationConvention = false;

    @JsonView(Views.List.class)
    private Boolean verificationAdministrative = false;

    @Column()
    private Boolean conversionEnContrat;

    private String commentaireStage;

    @Column
    private String adresseEtudiant;

    @Column
    private String codePostalEtudiant;

    @Column
    private String villeEtudiant;

    @Column
    private String paysEtudiant;

    @Column
    private String courrielPersoEtudiant;

    @Column
    private String telEtudiant;

    @Column
    private String telPortableEtudiant;

    @ManyToOne
    @JoinColumn(name = "idIndemnisation")
    private Indemnisation indemnisation;

    @Column
    private String montantGratification;

    private String fonctionsEtTaches;

    private String details;

    @JsonView(Views.List.class)
    @Column
    private String annee;

    @Column
    private String temConfSujetTeme;

    @Column
    private String nbHeuresHebdo;

    private String modeEncadreSuivi;

    @ManyToOne
    @JoinColumn(name = "idModeVersGratification")
    private ModeVersGratification modeVersGratification;

    private String avantagesNature;

    @ManyToOne
    @JoinColumn(name = "idNatureTravail")
    private NatureTravail natureTravail;

    @ManyToOne
    @JoinColumn(name = "idModeValidationStage")
    private ModeValidationStage modeValidationStage;

    @Column
    private String codeElp;

    @Column
    private String libelleELP;

    private BigDecimal creditECTS;

    private String travailNuitFerie;

    @Column()
    private Integer dureeStage;

    @Column
    private String nomEtabRef;

    @Column
    private String adresseEtabRef;

    @Column
    private String nomSignataireComposante;

    @Column
    private String qualiteSignataire;

    @Column
    private String libelleCPAM;

    @JsonView(Views.List.class)
    @Column
    private String regionCPAM;

    @JsonView(Views.List.class)
    @Column
    private String adresseCPAM;

    @Column
    private String dureeExceptionnelle;

    @ManyToOne
    @JoinColumn(name = "idUniteDureeExceptionnelle")
    private UniteDuree uniteDureeExceptionnelle;

    @ManyToOne
    @JoinColumn(name = "idUniteGratification")
    private UniteGratification uniteGratification;

    @Column
    private String codeFinalite;

    @Column
    private String libelleFinalite;

    @Column
    private String codeCursusLMD;

    private Boolean priseEnChargeFraisMission;

    @Column
    private String codeRGI;

    @Column
    private String loginValidation;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateValidation;

    @Column
    private String loginSignature;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSignature;

    @JsonView(Views.List.class)
    private Boolean envoiMailEtudiant;

    @JsonView(Views.List.class)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnvoiMailEtudiant;

    @JsonView(Views.List.class)
    private Boolean envoiMailTuteurPedago;

    @JsonView(Views.List.class)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnvoiMailTuteurPedago;

    @JsonView(Views.List.class)
    private Boolean envoiMailTuteurPro;

    @JsonView(Views.List.class)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnvoiMailTuteurPro;

    private String nbConges;
    private String competences;

    @ManyToOne
    @JoinColumn(name = "idUniteDureeGratification")
    private UniteDuree uniteDureeGratification;

    @Column
    private String monnaieGratification;

    @Column
    private String volumeHoraireFormation;

    @Column
    private String typePresence;

    @ManyToOne
    @JoinColumn(name = "idDevise")
    private Devise devise;

    @Column(nullable = false)
    private boolean validationCreation = false;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateValidationCreation;

    @JsonIgnore
    @Column(nullable = false)
    private boolean creationEnMasse = false;

    @ManyToOne
    @JoinColumn(name = "idPays")
    private Pays paysConvention;

    @Column
    private Boolean horairesReguliers;

    @Column
    private Boolean gratificationStage;

    @Column
    private Boolean confidentiel;

    @JsonView(Views.List.class)
    @OneToMany(mappedBy = "convention", cascade = {CascadeType.REMOVE})
    private List<Avenant> avenants = new ArrayList<>();

    @OneToOne(mappedBy = "convention", cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.PERSIST})
    private ConventionNomenclature nomenclature;

    @JsonIgnore
    @OneToMany(mappedBy = "convention", cascade = {CascadeType.REMOVE})
    private List<HistoriqueValidation> historiqueValidations = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "convention", cascade = {CascadeType.REMOVE})
    private List<PeriodeInterruptionStage> periodeInterruptionStages = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "convention", cascade = {CascadeType.REMOVE})
    private List<ReponseEvaluation> reponseEvaluations = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "convention", cascade = {CascadeType.REMOVE})
    private List<ReponseSupplementaire> reponseSupplementaires = new ArrayList<>();

    @JsonView(Views.List.class)
    @OneToOne(mappedBy = "convention", cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.PERSIST},fetch = FetchType.LAZY)
    private ReponseEvaluation reponseEvaluation;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnvoiSignature;

    @Column(length = 255)
    private String documentId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSignatureEtudiant;

    @Column
    private Date dateDepotEtudiant;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSignatureEnseignant;

    @Column
    private Date dateDepotEnseignant;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSignatureTuteur;

    @Column
    private Date dateDepotTuteur;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSignatureSignataire;

    @Column
    private Date dateDepotSignataire;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSignatureViseur;

    @Column
    private Date dateDepotViseur;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateActualisationSignature;

    @JsonView(Views.List.class)
    @Transient
    private String lieuStage;

    @JsonView(Views.List.class)
    @Transient
    private boolean depasseDelaiValidation = false;

    @Transient
    private String dureeExceptionnellePeriode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public void setEtudiant(Etudiant etudiant) {
        this.etudiant = etudiant;
    }

    public CentreGestion getCentreGestion() {
        return centreGestion;
    }

    public void setCentreGestion(CentreGestion centreGestion) {
        this.centreGestion = centreGestion;
    }

    public Ufr getUfr() {
        return ufr;
    }

    public void setUfr(Ufr ufr) {
        this.ufr = ufr;
    }

    public Etape getEtape() {
        return etape;
    }

    public void setEtape(Etape etape) {
        this.etape = etape;
    }

    public String getCodeDepartement() {
        return codeDepartement;
    }

    public void setCodeDepartement(String codeDepartement) {
        this.codeDepartement = codeDepartement;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public Contact getSignataire() {
        return signataire;
    }

    public void setSignataire(Contact signataire) {
        this.signataire = signataire;
    }

    public TypeConvention getTypeConvention() {
        return typeConvention;
    }

    public void setTypeConvention(TypeConvention typeConvention) {
        this.typeConvention = typeConvention;
    }

    public Offre getOffre() {
        return offre;
    }

    public void setOffre(Offre offre) {
        this.offre = offre;
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

    public NbJoursHebdoEnum getNbJoursHebdo() {
        return nbJoursHebdo;
    }

    public void setNbJoursHebdo(NbJoursHebdoEnum nbJoursHebdo) {
        this.nbJoursHebdo = nbJoursHebdo;
    }

    public TempsTravail getTempsTravail() {
        return tempsTravail;
    }

    public void setTempsTravail(TempsTravail tempsTravail) {
        this.tempsTravail = tempsTravail;
    }

    public String getCommentaireDureeTravail() {
        return commentaireDureeTravail;
    }

    public void setCommentaireDureeTravail(String commentaireDureeTravail) {
        this.commentaireDureeTravail = commentaireDureeTravail;
    }

    public LangueConvention getLangueConvention() {
        return langueConvention;
    }

    public void setLangueConvention(LangueConvention langueConvention) {
        this.langueConvention = langueConvention;
    }

    public OrigineStage getOrigineStage() {
        return origineStage;
    }

    public void setOrigineStage(OrigineStage origineStage) {
        this.origineStage = origineStage;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
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

    public Boolean getVerificationAdministrative() {
        return verificationAdministrative;
    }

    public void setVerificationAdministrative(Boolean verificationAdministrative) {
        this.verificationAdministrative = verificationAdministrative;
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

    public Indemnisation getIndemnisation() {
        return indemnisation;
    }

    public void setIndemnisation(Indemnisation indemnisation) {
        this.indemnisation = indemnisation;
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

    public String getTemConfSujetTeme() {
        return temConfSujetTeme;
    }

    public void setTemConfSujetTeme(String temConfSujetTeme) {
        this.temConfSujetTeme = temConfSujetTeme;
    }

    public String getNbHeuresHebdo() {
        return nbHeuresHebdo != null ? nbHeuresHebdo.replaceAll("[a-zA-Z ]*", "") : null;
    }

    public void setNbHeuresHebdo(String nbHeuresHebdo) {
        this.nbHeuresHebdo = nbHeuresHebdo;
    }

    public String getModeEncadreSuivi() {
        return modeEncadreSuivi;
    }

    public void setModeEncadreSuivi(String modeEncadreSuivi) {
        this.modeEncadreSuivi = modeEncadreSuivi;
    }

    public ModeVersGratification getModeVersGratification() {
        return modeVersGratification;
    }

    public void setModeVersGratification(ModeVersGratification modeVersGratification) {
        this.modeVersGratification = modeVersGratification;
    }

    public String getAvantagesNature() {
        return avantagesNature;
    }

    public void setAvantagesNature(String avantagesNature) {
        this.avantagesNature = avantagesNature;
    }

    public NatureTravail getNatureTravail() {
        return natureTravail;
    }

    public void setNatureTravail(NatureTravail natureTravail) {
        this.natureTravail = natureTravail;
    }

    public ModeValidationStage getModeValidationStage() {
        return modeValidationStage;
    }

    public void setModeValidationStage(ModeValidationStage modeValidationStage) {
        this.modeValidationStage = modeValidationStage;
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

    public String getRegionCPAM() {
        return regionCPAM;
    }

    public void setRegionCPAM(String regionCPAM) {
        this.regionCPAM = regionCPAM;
    }

    public String getAdresseCPAM() {
        return adresseCPAM;
    }

    public void setAdresseCPAM(String adresseCPAM) {
        this.adresseCPAM = adresseCPAM;
    }

    public String getDureeExceptionnelle() {
        return dureeExceptionnelle;
    }

    public void setDureeExceptionnelle(String dureeExceptionnelle) {
        this.dureeExceptionnelle = dureeExceptionnelle;
    }

    public UniteDuree getUniteDureeExceptionnelle() {
        return uniteDureeExceptionnelle;
    }

    public void setUniteDureeExceptionnelle(UniteDuree uniteDureeExceptionnelle) {
        this.uniteDureeExceptionnelle = uniteDureeExceptionnelle;
    }

    public UniteGratification getUniteGratification() {
        return uniteGratification;
    }

    public void setUniteGratification(UniteGratification uniteGratification) {
        this.uniteGratification = uniteGratification;
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

    public UniteDuree getUniteDureeGratification() {
        return uniteDureeGratification;
    }

    public void setUniteDureeGratification(UniteDuree uniteDureeGratification) {
        this.uniteDureeGratification = uniteDureeGratification;
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

    public Devise getDevise() {
        return devise;
    }

    public void setDevise(Devise devise) {
        this.devise = devise;
    }

    public boolean isValidationCreation() {
        return validationCreation;
    }

    public void setValidationCreation(boolean validationCreation) {
        this.validationCreation = validationCreation;
    }

    public Date getDateValidationCreation() {
        return dateValidationCreation;
    }

    public void setDateValidationCreation(Date dateValidationCreation) {
        this.dateValidationCreation = dateValidationCreation;
    }

    public boolean isCreationEnMasse() {
        return creationEnMasse;
    }

    public void setCreationEnMasse(boolean creationEnMasse) {
        this.creationEnMasse = creationEnMasse;
    }

    public Pays getPaysConvention() {
        return paysConvention;
    }

    public void setPaysConvention(Pays paysConvention) {
        this.paysConvention = paysConvention;
    }

    public Boolean isHorairesReguliers() {
        return horairesReguliers;
    }

    public void setHorairesReguliers(Boolean horairesReguliers) {
        this.horairesReguliers = horairesReguliers;
    }

    public Boolean getGratificationStage() {
        return gratificationStage;
    }

    public void setGratificationStage(Boolean gratificationStage) {
        this.gratificationStage = gratificationStage;
    }

    public Boolean getConfidentiel() {
        return confidentiel;
    }

    public void setConfidentiel(Boolean confidentiel) {
        this.confidentiel = confidentiel;
    }

    public List<Avenant> getAvenants() {
        return avenants;
    }

    public void setAvenants(List<Avenant> avenants) {
        this.avenants = avenants;
    }

    public ConventionNomenclature getNomenclature() {
        return nomenclature;
    }

    public void setNomenclature(ConventionNomenclature nomenclature) {
        this.nomenclature = nomenclature;
        this.nomenclature.setConvention(this);
    }

    public ReponseEvaluation getReponseEvaluation() {
        return reponseEvaluation;
    }

    public void setReponseEvaluation(ReponseEvaluation reponseEvaluation) {
        this.reponseEvaluation = reponseEvaluation;
    }

    public Date getDateEnvoiSignature() {
        return dateEnvoiSignature;
    }

    public void setDateEnvoiSignature(Date dateEnvoiSignature) {
        this.dateEnvoiSignature = dateEnvoiSignature;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Date getDateSignatureEtudiant() {
        return dateSignatureEtudiant;
    }

    public void setDateSignatureEtudiant(Date dateSignatureEtudiant) {
        this.dateSignatureEtudiant = dateSignatureEtudiant;
    }

    public Date getDateDepotEtudiant() {
        return dateDepotEtudiant;
    }

    public void setDateDepotEtudiant(Date dateDepotEtudiant) {
        this.dateDepotEtudiant = dateDepotEtudiant;
    }

    public Date getDateSignatureEnseignant() {
        return dateSignatureEnseignant;
    }

    public void setDateSignatureEnseignant(Date dateSignatureEnseignant) {
        this.dateSignatureEnseignant = dateSignatureEnseignant;
    }

    public Date getDateDepotEnseignant() {
        return dateDepotEnseignant;
    }

    public void setDateDepotEnseignant(Date dateDepotEnseignant) {
        this.dateDepotEnseignant = dateDepotEnseignant;
    }

    public Date getDateSignatureTuteur() {
        return dateSignatureTuteur;
    }

    public void setDateSignatureTuteur(Date dateSignatureTuteur) {
        this.dateSignatureTuteur = dateSignatureTuteur;
    }

    public Date getDateDepotTuteur() {
        return dateDepotTuteur;
    }

    public void setDateDepotTuteur(Date dateDepotTuteur) {
        this.dateDepotTuteur = dateDepotTuteur;
    }

    public Date getDateSignatureSignataire() {
        return dateSignatureSignataire;
    }

    public void setDateSignatureSignataire(Date dateSignatureSignataire) {
        this.dateSignatureSignataire = dateSignatureSignataire;
    }

    public Date getDateDepotSignataire() {
        return dateDepotSignataire;
    }

    public void setDateDepotSignataire(Date dateDepotSignataire) {
        this.dateDepotSignataire = dateDepotSignataire;
    }

    public Date getDateSignatureViseur() {
        return dateSignatureViseur;
    }

    public void setDateSignatureViseur(Date dateSignatureViseur) {
        this.dateSignatureViseur = dateSignatureViseur;
    }

    public Date getDateDepotViseur() {
        return dateDepotViseur;
    }

    public void setDateDepotViseur(Date dateDepotViseur) {
        this.dateDepotViseur = dateDepotViseur;
    }

    public Date getDateActualisationSignature() {
        return dateActualisationSignature;
    }

    public void setDateActualisationSignature(Date dateActualisationSignature) {
        this.dateActualisationSignature = dateActualisationSignature;
    }

    public String getLieuStage() {
        if (getService() != null) {
            return getService().getNom() + " " + getService().getCommune() + (getService().getPays() != null ? " " + getService().getPays().getLib() : "");
        }
        return null;
    }

    public void setLieuStage(String lieuStage) {
        this.lieuStage = lieuStage;
    }

    public String getDureeExceptionnellePeriode() {
        if (this.getNbHeuresHebdo() != null && !this.getNbHeuresHebdo().equals("")
                && this.getDureeExceptionnelle() != null && !this.getDureeExceptionnelle().equals("")) {
            this.setDureeExceptionnellePeriode(PeriodeService.calculPeriodeOuvree(Float.parseFloat(this.getNbHeuresHebdo()), Float.parseFloat(this.getDureeExceptionnelle())));
        }
        return dureeExceptionnellePeriode;
    }

    public void setDureeExceptionnellePeriode(String dureeExceptionnellePeriode) {
        this.dureeExceptionnellePeriode = dureeExceptionnellePeriode;
    }

    public List<HistoriqueValidation> getHistoriqueValidations() {
        return historiqueValidations;
    }

    public void setHistoriqueValidations(List<HistoriqueValidation> historiqueValidations) {
        this.historiqueValidations = historiqueValidations;
    }

    public List<PeriodeInterruptionStage> getPeriodeInterruptionStages() {
        if (periodeInterruptionStages != null) {
            // Ordonne par ordre de début asc
            periodeInterruptionStages.sort(Comparator.comparing(PeriodeInterruptionStage::getDateDebutInterruption));
        }
        return periodeInterruptionStages;
    }

    public void setPeriodeInterruptionStages(List<PeriodeInterruptionStage> periodeInterruptionStages) {
        this.periodeInterruptionStages = periodeInterruptionStages;
    }

    public List<ReponseEvaluation> getReponseEvaluations() {
        return reponseEvaluations;
    }

    public void setReponseEvaluations(List<ReponseEvaluation> reponseEvaluations) {
        this.reponseEvaluations = reponseEvaluations;
    }

    public List<ReponseSupplementaire> getReponseSupplementaires() {
        return reponseSupplementaires;
    }

    public void setReponseSupplementaires(List<ReponseSupplementaire> reponseSupplementaires) {
        this.reponseSupplementaires = reponseSupplementaires;
    }

    public boolean isDepasseDelaiValidation() {
        CentreGestion centreGestion = getCentreGestion();
        if (centreGestion != null) {
            Date now = new Date();
            Calendar calendarNow = Calendar.getInstance();
            calendarNow.setTime(now);
            calendarNow.set(Calendar.HOUR_OF_DAY, 0);
            calendarNow.set(Calendar.MINUTE, 0);
            calendarNow.set(Calendar.SECOND, 0);
            calendarNow.set(Calendar.MILLISECOND, 0);
            now = calendarNow.getTime();

            Date dateDebutStage = getDateDebutStage();
            if (dateDebutStage != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateDebutStage);
                calendar.add(Calendar.DAY_OF_MONTH, centreGestion.getDelaiAlerteConvention() * -1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Date dateAlerte = calendar.getTime();
                if (now.after(dateAlerte) || now.compareTo(dateAlerte) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setValeurNomenclature() {
        ConventionNomenclature conventionNomenclature = this.getNomenclature();
        if (conventionNomenclature == null) {
            conventionNomenclature = new ConventionNomenclature();
            conventionNomenclature.setConvention(this);
        }
        conventionNomenclature.setLangueConvention(this.getLangueConvention().getLibelle());
        conventionNomenclature.setDevise(this.getDevise() != null ? this.getDevise().getLibelle() : null);
        conventionNomenclature.setModeValidationStage(this.getModeValidationStage() != null ? this.getModeValidationStage().getLibelle() : null);
        conventionNomenclature.setModeVersGratification(this.getModeVersGratification() != null ? this.getModeVersGratification().getLibelle() : null);
        conventionNomenclature.setNatureTravail(this.getNatureTravail() != null ? this.getNatureTravail().getLibelle() : null);
        conventionNomenclature.setOrigineStage(this.getOrigineStage() != null ? this.getOrigineStage().getLibelle() : null);
        conventionNomenclature.setTempsTravail(this.getTempsTravail() != null ? this.getTempsTravail().getLibelle() : null);
        conventionNomenclature.setTheme(this.getTheme() != null ? this.getTheme().getLibelle() : null);
        conventionNomenclature.setTypeConvention(this.getTypeConvention().getLibelle());
        conventionNomenclature.setUniteDureeExceptionnelle(this.getUniteDureeExceptionnelle() != null ? this.getUniteDureeExceptionnelle().getLibelle() : null);
        conventionNomenclature.setUniteDureeGratification(this.getUniteDureeGratification() != null ? this.getUniteDureeGratification().getLibelle() : null);
        conventionNomenclature.setUniteGratification(this.getUniteGratification() != null ? this.getUniteGratification().getLibelle() : null);
        this.setNomenclature(conventionNomenclature);
    }

    @Override
    public String getExportValue(String key) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String value = "";
        switch (key) {
            case "id":
                value = String.valueOf(getId());
                break;
            case "numEtudiant":
                if (getEtudiant() != null) {
                    value = getEtudiant().getNumEtudiant();
                }
                break;
            case "etudiant":
                if (getEtudiant() != null) {
                    value = getEtudiant().getPrenom() + " " + getEtudiant().getNom();
                }
                break;
            case "courrielPersoEtudiant":
                value = getCourrielPersoEtudiant();
                break;
            case "mailUniEtudiant":
                if (getEtudiant() != null) {
                    value = getEtudiant().getMail();
                }
                break;
            case "telEtudiant":
                value = getTelEtudiant();
                break;
            case "telPortableEtudiant":
                value = getTelPortableEtudiant();
                break;
            case "codeUFR":
                if (getUfr() != null) {
                    value = getUfr().getId().getCode();
                }
                break;
            case "ufr":
                if (getUfr() != null) {
                    value = getUfr().getLibelle();
                }
                break;
            case "codeDepartement":
                value = getCodeDepartement();
                break;
            case "codeEtape":
                if (getEtape() != null) {
                    value = getEtape().getId().getCode();
                }
                break;
            case "etape":
                if (getEtape() != null) {
                    value = getEtape().getLibelle();
                }
                break;
            case "dateDebutStage":
                if (getDateDebutStage() != null) {
                    value = df.format(getDateDebutStage());
                }
                break;
            case "dateFinStage":
                if (getDateFinStage() != null) {
                    value = df.format(getDateFinStage());
                }
                break;
            case "interruptionStage":
                if (getInterruptionStage() != null) {
                    value = getInterruptionStage()?"Oui":"Non";
                }
                break;
            case "theme":
                if (getTheme() != null) {
                    value = getTheme().getLibelle();
                }
                break;
            case "sujetStage":
                value = getSujetStage();
                break;
            case "fonctionsEtTaches":
                value = getFonctionsEtTaches();
                break;
            case "details":
                value = getDetails();
                break;
            case "dureeExceptionnelle":
                value = getDureeExceptionnelle();
                break;
            case "nbJoursHebdo":
                if (getNbJoursHebdo() != null) {
                    value = getNbJoursHebdo().getValue();
                }
                break;
            case "nbHeuresHebdo":
                value = getNbHeuresHebdo();
                break;
            case "gratification":
                if (getUniteGratification() != null) {
                    value = getMontantGratification() + " " + getUniteGratification().getLibelle();
                }
                break;
            case "uniteDuree":
                if (getUniteDureeGratification() != null) {
                    value = getUniteDureeGratification().getLibelle();
                }
                break;
            case "validationPedagogique":
                value = getValidationPedagogique() != null && getValidationPedagogique() ? "Oui" : "Non";
                break;
            case "verificationAdministrative":
                value = getVerificationAdministrative() != null && getVerificationAdministrative() ? "Oui" : "Non";
                break;
            case "validationConvention":
                value = getValidationConvention() != null && getValidationConvention() ? "Oui" : "Non";
                break;
            case "enseignant":
                if (getEnseignant() != null) {
                    value = getEnseignant().getPrenom() + " " + getEnseignant().getNom();
                }
                break;
            case "mailEnseignant":
                if (getEnseignant() != null) {
                    value = getEnseignant().getMail();
                }
                break;
            case "signataire":
                if (getSignataire() != null) {
                    value = getSignataire().getPrenom() + " " + getSignataire().getNom();
                }
                break;
            case "mailSignataire":
                if (getSignataire() != null) {
                    value = getSignataire().getMail();
                }
                break;
            case "fonctionSignataire":
                if (getSignataire() != null) {
                    value = getSignataire().getFonction();
                }
                break;
            case "annee":
                value = getAnnee();
                break;
            case "typeConvention":
                if (getTypeConvention() != null) {
                    value = getTypeConvention().getLibelle();
                }
                break;
            case "commentaireStage":
                value = getCommentaireStage();
                break;
            case "commentaireDureeTravail":
                value = getCommentaireDureeTravail();
                break;
            case "codeElp":
                value = getCodeElp();
                break;
            case "libelleELP":
                value = getLibelleELP();
                break;
            case "codeSexeEtu":
                if (getEtudiant() != null) {
                    value = getEtudiant().getCodeSexe();
                }
                break;
            case "avantageNature":
                value = getAvantagesNature();
                break;
            case "adresseEtudiant":
                value = getAdresseEtudiant();
                break;
            case "codePostalEtudiant":
                value = getCodePostalEtudiant();
                break;
            case "paysEtudiant":
                value = getPaysEtudiant();
                break;
            case "villeEtudiant":
                value = getVilleEtudiant();
                break;
            case "avenant":
                if (getAvenants() != null) {
                    value = getAvenants().isEmpty() ? "Non" : "Oui";
                break;
                }
            case "dateCreation":
                if (getDateCreation() != null) {
                    value = df.format(getDateCreation());
                }
                break;
            case "dateModif":
                if (getDateModif() != null) {
                    value = df.format(getDateModif());
                }
                break;
            case "structure":
                if (getStructure() != null) {
                    value = getStructure().getRaisonSociale();
                }
                break;
            case "structureSiret":
                if (getStructure() != null) {
                    value = getStructure().getNumeroSiret();
                }
                break;
            case "structureAdresse":
                if (getStructure() != null) {
                    value = getStructure().getVoie();
                }
                break;
            case "structureCP":
                if (getStructure() != null) {
                    value = getStructure().getCodePostal();
                }
                break;
            case "structureCommune":
                if (getStructure() != null) {
                    value = getStructure().getCommune();
                }
                break;
            case "structurePays":
                if (getStructure() != null) {
                    if (getStructure().getPays() != null) {
                        value = getStructure().getPays().getLib();
                    }
                }
                break;
            case "structureStatutJuridique":
                if (getStructure() != null) {
                    if (getStructure().getStatutJuridique() != null) {
                        value = getStructure().getStatutJuridique().getLibelle();
                    }
                }
                break;
            case "structureType":
                if (getStructure() != null) {
                    if (getStructure().getTypeStructure() != null) {
                        value = getStructure().getTypeStructure().getLibelle();
                    }
                }
                break;
            case "structureEffectif":
                if (getStructure() != null) {
                    if (getStructure().getEffectif() != null) {
                        value = getStructure().getEffectif().getLibelle();
                    }
                }
                break;
            case "structureNAF":
                if (getStructure() != null) {
                    if (getStructure().getNafN5() != null) {
                        value = getStructure().getNafN5().getLibelle();
                    }
                }
                break;
            case "structurePhone":
                if (getStructure() != null) {
                    value = getStructure().getTelephone();
                }
                break;
            case "structureMail":
                if (getStructure() != null) {
                    value = getStructure().getMail();
                }
                break;
            case "structureSiteWeb":
                if (getStructure() != null) {
                    value = getStructure().getSiteWeb();
                }
                break;
            case "service":
                if (getService() != null) {
                    value = getService().getNom();
                }
                break;
            case "serviceAdresse":
                if (getService() != null) {
                    value = getService().getVoie();
                }
                break;
            case "serviceCP":
                if (getService() != null) {
                    value = getService().getCodePostal();
                }
                break;
            case "serviceCommune":
                if (getService() != null) {
                    value = getService().getCommune();
                }
                break;
            case "servicePays":
                if (getService() != null) {
                    if (getService().getPays() != null) {
                        value = getService().getPays().getLib();
                    }
                }
                break;
            case "tuteur":
                if (getContact() != null) {
                    value = getContact().getNom() + " " + getContact().getPrenom();
                }
                break;
            case "tuteurMail":
                if (getContact() != null) {
                    value = getContact().getMail();
                }
                break;
            case "tuteurPhone":
                if (getContact() != null) {
                    value = getContact().getTel();
                }
                break;
            case "tuteurFonction":
                if (getContact() != null) {
                    value = getContact().getFonction();
                }
                break;
            case "lieuStage":
                value = getLieuStage();
                break;
            default:
                break;
        }
        return value;
    }

    @Transient
    public boolean isAllSignedDateSetted() {
        return getDateDepotEtudiant() != null && getDateSignatureEtudiant() != null &&
                getDateDepotEnseignant() != null && getDateSignatureEnseignant() != null &&
                getDateDepotTuteur() != null && getDateSignatureTuteur() != null &&
                getDateDepotSignataire() != null && getDateSignatureSignataire() != null &&
                getDateDepotViseur() != null && getDateSignatureViseur() != null;
    }
}
