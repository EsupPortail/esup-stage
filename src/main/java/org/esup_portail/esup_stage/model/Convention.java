package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.NbJoursHebdoEnum;
import org.esup_portail.esup_stage.service.PeriodeService;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Entity
@Table(name = "Convention")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
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
    @ManyToOne
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idService")
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idContact")
    private Contact contact;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumn(name = "idSignataire")
    private Contact signataire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTypeConvention")
    private TypeConvention typeConvention;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTempsTravail")
    private TempsTravail tempsTravail;

    private String commentaireDureeTravail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codeLangueConvention")
    private LangueConvention langueConvention;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idOrigineStage")
    private OrigineStage origineStage;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @ManyToOne(fetch = FetchType.LAZY)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idModeVersGratification")
    private ModeVersGratification modeVersGratification;

    private String avantagesNature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idNatureTravail")
    private NatureTravail natureTravail;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUniteDureeExceptionnelle")
    private UniteDuree uniteDureeExceptionnelle;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUniteDureeGratification")
    private UniteDuree uniteDureeGratification;

    @Column
    private String monnaieGratification;

    @Column
    private String volumeHoraireFormation;

    @Column
    private String typePresence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDevise")
    private Devise devise;

    @Column(nullable = false)
    private boolean validationCreation = false;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateValidationCreation;

    @JsonIgnore
    @Column(nullable = false)
    private boolean creationEnMasse = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPays")
    private Pays paysConvention;

    @Column
    private Boolean horairesReguliers;

    @Column
    private Boolean gratificationStage;

    @Column
    private Boolean confidentiel;

    @JsonView(Views.List.class)
    @OneToMany(mappedBy = "convention", cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
    private List<Avenant> avenants = new ArrayList<>();

    @OneToOne(mappedBy = "convention", cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.PERSIST})
    private ConventionNomenclature nomenclature;

    @JsonIgnore
    @OneToMany(mappedBy = "convention", cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
    private List<HistoriqueValidation> historiqueValidations = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "convention", cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
    private List<PeriodeInterruptionStage> periodeInterruptionStages = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "convention", cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
    private List<ReponseEvaluation> reponseEvaluations = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "convention", cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
    private List<ReponseSupplementaire> reponseSupplementaires = new ArrayList<>();

    @JsonView(Views.List.class)
    @OneToOne(mappedBy = "convention", cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
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

    @Column
    private String loginEnvoiSignature;

    @JsonView(Views.List.class)
    @Transient
    private String lieuStage;

    @JsonView(Views.List.class)
    @Transient
    private boolean depasseDelaiValidation = false;

    @Column(name = "dureeExceptionnellePeriode")
    private String dureeExceptionnellePeriode;

    @JsonIgnore
    @OneToMany(mappedBy = "convention", cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
    private List<PeriodeStage> PeriodeStage;

    @Column
    private boolean temConventionSignee;

    public void setNomenclature(ConventionNomenclature nomenclature) {
        this.nomenclature = nomenclature;
        this.nomenclature.setConvention(this);
    }

    public String getLieuStage() {
        if (getService() != null) {
            return getService().getNom() + " " + getService().getCommune() + (getService().getPays() != null ? " " + getService().getPays().getLib() : "");
        }
        return null;
    }

    public String getDureeExceptionnellePeriode() {
        if(dureeExceptionnellePeriode == null){
            if (this.getNbHeuresHebdo() != null && !this.getNbHeuresHebdo().equals("")
                    && this.getDureeExceptionnelle() != null && !this.getDureeExceptionnelle().equals("")) {
                return PeriodeService.calculPeriodeOuvree(Float.parseFloat(this.getNbHeuresHebdo()), Float.parseFloat(this.getDureeExceptionnelle()));
            }
        }
        return dureeExceptionnellePeriode;
    }

    public List<PeriodeInterruptionStage> getPeriodeInterruptionStages() {
        if (periodeInterruptionStages != null) {
            // Ordonne par ordre de début asc
            periodeInterruptionStages.sort(Comparator.comparing(PeriodeInterruptionStage::getDateDebutInterruption));
        }
        return periodeInterruptionStages;
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
                return now.after(dateAlerte) || now.compareTo(dateAlerte) == 0;
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
            case "etudiantNom":
                if (getEtudiant() != null) {
                    value = getEtudiant().getNom();
                }
                break;
            case "etudiantPrenom":
                if (getEtudiant() != null) {
                    value = getEtudiant().getPrenom();
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
                    value = getInterruptionStage() ? "Oui" : "Non";
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
