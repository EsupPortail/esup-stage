package org.esup_portail.esup_stage.service.impression.context;

import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.esup_portail.esup_stage.model.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ImpressionContext {
    private ConventionContext convention = new ConventionContext();
    private CentreGestionContext centreGestion = new CentreGestionContext();
    private ContactContext contact = new ContactContext();
    private EnseignantContext enseignant = new EnseignantContext();
    private EtudiantContext etudiant = new EtudiantContext();
    private ServiceContext service = new ServiceContext();
    private SignataireContext signataire = new SignataireContext();
    private StructureContext structure = new StructureContext();
    private AvenantContext avenant = new AvenantContext();
    private ReponseEvaluationContext reponse = new ReponseEvaluationContext();
    private FicheEvaluationContext ficheEvaluation = new FicheEvaluationContext();
    private List<QuestionSupplementaireContext> questionsSupplementaires = new ArrayList<>();
    private List<ReponseSupplementaireContext> reponsesSupplementaires = new ArrayList<>();

    public ImpressionContext(Convention convention, Avenant avenant, CentreGestion centreEtablissement, List<QuestionSupplementaire> questionSupplementaires) {
        if (convention != null) {
            this.convention = new ConventionContext(convention, centreEtablissement);
            this.centreGestion = new CentreGestionContext(convention.getCentreGestion(), centreEtablissement);
            this.contact = new ContactContext(convention.getContact());
            this.enseignant = new EnseignantContext(convention.getEnseignant());
            this.etudiant = new EtudiantContext(convention.getEtudiant());
            this.service = new ServiceContext(convention.getService());
            this.signataire = new SignataireContext(convention.getSignataire());
            this.structure = new StructureContext(convention.getStructure());
            FicheEvaluation ficheEvaluation = convention.getCentreGestion().getFicheEvaluation();
            if (ficheEvaluation != null) {
                for (QuestionSupplementaire question : questionSupplementaires) {
                    this.questionsSupplementaires.add(new QuestionSupplementaireContext(question));
                }
            }
            for (ReponseSupplementaire reponse : convention.getReponseSupplementaires()) {
                this.reponsesSupplementaires.add(new ReponseSupplementaireContext(reponse));
            }
        }
        if (avenant != null) {
            this.avenant = new AvenantContext(avenant);
        }
    }

    @Data
    @NoArgsConstructor
    public static class ConventionContext {
        private String id;
        private String adresseEtabRef;
        private String adresseEtudiant;
        private String annee;
        private String avantagesNature;
        private String codeCursusLMD;
        private String codeDepartement;
        private String codePostalEtudiant;
        private String commentaireDureeTravail;
        private String courrielPersoEtudiant;
        private String creditECTS;
        private String dateDebutStage;
        private String dateFinStage;
        private String details;
        private String dureeStageHeure;
        private String dureeStageHeurePeriode;
        private String dureeStage;
        private String etapeLibelle;
        private String etapeCode;
        private String fonctionsEtTaches;
        private String interruptionStage;
        private String libelleCPAM;
        private String regionCPAM;
        private String adresseCPAM;
        private String libelleFinalite;
        private String modeEncadreSuivi;
        private String modeValidationStageLibelle;
        private String modeVersGratificationLibelle;
        private String montantGratification;
        private String deviseGratification;
        private String natureTravailLibelle;
        private String nbHeuresHebdo;
        private String nbConges;
        private String nbJoursHebdo;
        private String nomEtabRef;
        private String nomSignataireComposante;
        private String origineStageLibelle;
        private String paysEtudiant;
        private String qualiteSignataire;
        private String sujetStage;
        private String telEtudiant;
        private String telPortableEtudiant;
        private String tempsTravailLibelle;
        private String themeLibelle;
        private String travailNuitFerie;
        private String ufrLibelle;
        private String ufrCode;
        private String uniteGratificationLibelle;
        private String uniteDureeGratificationLibelle;
        private String villeEtudiant;
        private String volumeHoraireFormation;
        private String competences;
        private List<PeriodeInterruptionContext> periodesInterruptions = new ArrayList<>();
        private String typeConventionLibelle;
        private String langueConvention;
        private String confidentiel;
        private String lieuStage;
        private String conventionValidee;
        private String codeCaisse;
        private List<HoraireIrregulierContext> horaireIrregulier = new ArrayList<>();

        public ConventionContext(Convention convention, CentreGestion centreEtablissement) {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

            this.id = String.valueOf(convention.getId());
            this.adresseEtabRef = convention.getAdresseEtabRef() != null ? convention.getAdresseEtabRef() : centreEtablissement.getAdresseComplete();
            this.adresseEtudiant = convention.getAdresseEtudiant();
            this.annee = convention.getAnnee();
            this.avantagesNature = convention.getAvantagesNature();
            this.codeCursusLMD = convention.getCodeCursusLMD();
            this.codeDepartement = convention.getCodeDepartement();
            this.codePostalEtudiant = convention.getCodePostalEtudiant();
            this.commentaireDureeTravail = convention.getCommentaireDureeTravail();
            this.courrielPersoEtudiant = convention.getCourrielPersoEtudiant();
            this.creditECTS = String.valueOf(convention.getCreditECTS() != null ? convention.getCreditECTS() : "");
            this.dateDebutStage = convention.getDateDebutStage() != null ? df.format(convention.getDateDebutStage()) : null;
            this.dateFinStage = convention.getDateFinStage() != null ? df.format(convention.getDateFinStage()) : null;
            this.details = convention.getDetails();
            this.dureeStageHeure = convention.getDureeExceptionnelle();
            this.dureeStage = convention.getDureeStage() != null ? String.valueOf(convention.getDureeStage()) : "";
            this.etapeLibelle = convention.getEtape() != null ? convention.getEtape().getLibelle() : null;
            this.etapeCode = convention.getEtape() != null ? convention.getEtape().getId().getCode() : null;
            this.fonctionsEtTaches = convention.getFonctionsEtTaches();
            this.interruptionStage = (convention.getInterruptionStage() != null && convention.getInterruptionStage()) ? "Oui" : "Non";
            this.libelleCPAM = convention.getLibelleCPAM();
            this.regionCPAM = convention.getRegionCPAM();
            this.adresseCPAM = convention.getAdresseCPAM();
            this.libelleFinalite = convention.getLibelleFinalite();
            this.modeEncadreSuivi = convention.getModeEncadreSuivi();
            this.modeValidationStageLibelle = convention.getNomenclature() != null ? convention.getNomenclature().getModeValidationStage() : null;
            if (convention.getGratificationStage() != null && convention.getGratificationStage()) {
                this.modeVersGratificationLibelle = convention.getNomenclature() != null ? convention.getNomenclature().getModeVersGratification() : null;
                this.montantGratification = convention.getMontantGratification();
                this.uniteGratificationLibelle = convention.getNomenclature() != null ? convention.getNomenclature().getUniteGratification() : null;
                this.uniteDureeGratificationLibelle = convention.getNomenclature() != null ? convention.getNomenclature().getUniteDureeGratification() : null;
                this.deviseGratification = convention.getDevise() != null ? convention.getDevise().getLibelle() : null;
            }
            this.natureTravailLibelle = convention.getNomenclature() != null ? convention.getNomenclature().getNatureTravail() : null;
            this.nbHeuresHebdo = convention.getNbHeuresHebdo();
            this.nbConges = convention.getNbConges();
            this.nbJoursHebdo = convention.getNbJoursHebdo() != null ? convention.getNbJoursHebdo().getValue() : null;
            this.nomEtabRef = convention.getNomEtabRef() != null ? convention.getNomEtabRef() : centreEtablissement.getNomCentre();
            this.nomSignataireComposante = convention.getNomSignataireComposante();
            this.origineStageLibelle = convention.getNomenclature() != null ? convention.getNomenclature().getOrigineStage() : null;
            this.paysEtudiant = convention.getPaysEtudiant();
            this.qualiteSignataire = convention.getQualiteSignataire();
            this.sujetStage = convention.getSujetStage();
            this.telEtudiant = convention.getTelEtudiant();
            this.telPortableEtudiant = convention.getTelPortableEtudiant();
            this.tempsTravailLibelle = convention.getNomenclature() != null ? convention.getNomenclature().getTempsTravail() : null;
            this.themeLibelle = convention.getNomenclature() != null ? convention.getNomenclature().getTheme() : null;
            this.travailNuitFerie = convention.getTravailNuitFerie();
            this.ufrLibelle = convention.getUfr() != null ? convention.getUfr().getLibelle() : null;
            this.ufrCode = convention.getUfr() != null ? convention.getUfr().getId().getCode() : null;
            this.villeEtudiant = convention.getVilleEtudiant();
            this.volumeHoraireFormation = convention.getVolumeHoraireFormation();
            this.dureeStageHeurePeriode = convention.getDureeExceptionnellePeriode();
            this.competences = convention.getCompetences();
            for (PeriodeInterruptionStage periode : convention.getPeriodeInterruptionStages()) {
                this.periodesInterruptions.add(new PeriodeInterruptionContext(df.format(periode.getDateDebutInterruption()), df.format(periode.getDateFinInterruption())));
            }
            this.typeConventionLibelle = convention.getTypeConvention().getLibelle();
            this.langueConvention = convention.getLangueConvention() != null ? convention.getLangueConvention().getLibelle() : "";
            this.confidentiel = convention.getConfidentiel() != null && convention.getConfidentiel() ? "Oui" : "Non";
            this.lieuStage = convention.getLieuStage();

            Boolean validationConvention = null;
            if (centreEtablissement != null) {
                validationConvention = centreEtablissement.getValidationPedagogique() ? convention.getValidationPedagogique() : null;
                validationConvention = centreEtablissement.getValidationConvention() ? Boolean.valueOf(validationConvention != null ?
                        validationConvention && convention.getValidationConvention() : convention.getValidationConvention()) : validationConvention;
            }
            this.conventionValidee = validationConvention != null && validationConvention ? "Oui" : "Non";
            this.codeCaisse = convention.getLibelleCPAM();
            for (PeriodeStage periode : convention.getPeriodeStage()) {
                this.horaireIrregulier.add(new HoraireIrregulierContext(
                        df.format(periode.getDateDebut()),
                        df.format(periode.getDateFin()),
                        periode.getNbHeuresJournalieres()
                ));
            }

        }
    }

    @Data
    @NoArgsConstructor
    public static class CentreGestionContext {
        private String adresse;
        private String codePostal;
        private String codeUniversite;
        private String commune;
        private String mail;
        private String nomCentre;
        private String nomViseur;
        private String prenomViseur;
        private String qualiteViseur;
        private String telephone;
        private String voie;
        private String prenomPresidentEtab;
        private String nomPresidentEtab;
        private String qualitePresidentEtab;

        public CentreGestionContext(CentreGestion centreGestion, CentreGestion centreEtablissement) {
            this.adresse = centreGestion.getAdresse();
            this.codePostal = centreGestion.getCodePostal();
            this.codeUniversite = centreGestion.getCodeUniversite();
            this.commune = centreGestion.getCommune();
            this.mail = centreGestion.getMail();
            this.nomCentre = centreGestion.getNomCentre();
            this.nomViseur = centreGestion.getNomViseur();
            this.prenomViseur = centreGestion.getPrenomViseur();
            this.qualiteViseur = centreGestion.getQualiteViseur();
            this.telephone = centreGestion.getTelephone();
            this.voie = centreGestion.getVoie();
            if (centreEtablissement != null) {
                this.prenomPresidentEtab = centreEtablissement.getPrenomViseur();
                this.nomPresidentEtab = centreEtablissement.getNomViseur();
                this.qualitePresidentEtab = centreEtablissement.getQualiteViseur();
            }
        }
    }

    @Data
    @NoArgsConstructor
    public static class ContactContext {
        private String civiliteLibelle;
        private String fonction;
        private String mail;
        private String nom;
        private String prenom;
        private String tel;

        public ContactContext(Contact contact) {
            if (contact == null) { this.civiliteLibelle = this.fonction = this.mail = this.nom = this.prenom = this.tel = ""; return; }
            this.civiliteLibelle = contact.getCivilite() != null ? contact.getCivilite().getLibelle() : null;
            this.fonction = contact.getFonction();
            this.mail = contact.getMail();
            this.nom = contact.getNom();
            this.prenom = contact.getPrenom();
            this.tel = contact.getTel();
        }

    }

    @Data
    @NoArgsConstructor
    public static class EnseignantContext {
        private String affectationLibelle;
        private String bureau;
        private String nom;
        private String prenom;
        private String tel;
        private String mail;
        private String fonction;

        public EnseignantContext(Enseignant enseignant) {
            if (enseignant == null) { this.affectationLibelle = this.bureau = this.nom = this.prenom = this.tel = this.mail = this.fonction = ""; return; }
            this.affectationLibelle = enseignant.getAffectation() != null ? enseignant.getAffectation().getLibelle() : null;
            this.bureau = enseignant.getBureau();
            this.nom = enseignant.getNom();
            this.prenom = enseignant.getPrenom();
            this.tel = enseignant.getTel();
            this.mail = enseignant.getMail();
            this.fonction = enseignant.getTypePersonne();
        }
    }

    @Data
    @NoArgsConstructor
    public static class EtudiantContext {
        private String codeSexe;
        private String dateNais;
        private String identEtudiant;
        private String mail;
        private String nom;
        private String numEtudiant;
        private String prenom;
        private String prenom2;
        private String prenomEtatCivil;
        private String sexEtatCivil;


        public EtudiantContext(Etudiant etudiant) {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

            this.codeSexe = etudiant.getCodeSexe();
            this.dateNais = etudiant.getDateNais() != null ? df.format(etudiant.getDateNais()) : null;
            this.identEtudiant = etudiant.getIdentEtudiant();
            this.mail = etudiant.getMail();
            this.nom = etudiant.getNom();
            this.numEtudiant = etudiant.getNumEtudiant();
            this.prenom = etudiant.getPrenom();
            this.prenom2 = etudiant.getPrenom2();
            this.prenomEtatCivil = etudiant.getPrenomEtatCivil();
            this.sexEtatCivil = etudiant.getSexEtatCivil();
        }
    }

    @Data
    public static class ServiceContext {
        private String codePostal;
        private String commune;
        private String nom;
        private String paysLibelle;
        private String voie;
        private String batiment;

        public ServiceContext() {
        }

        public ServiceContext(Service service) {
            this.codePostal = service.getCodePostal();
            this.commune = service.getCommune();
            this.nom = service.getNom();
            this.paysLibelle = service.getPays() != null ? service.getPays().getLib() : null;
            this.voie = service.getVoie();
            this.batiment = service.getBatimentResidence();
        }
    }

    @Data
    @NoArgsConstructor
    public static class SignataireContext {
        private String civiliteLibelle;
        private String fonction;
        private String mail;
        private String nom;
        private String prenom;
        private String tel;

        public SignataireContext(Contact signataire) {
            if (signataire == null) {
                this.civiliteLibelle = "";
                this.fonction = "";
                this.mail = "";
                this.nom = "";
                this.prenom = "";
                this.tel = "";
            } else {
                this.civiliteLibelle = signataire.getCivilite() != null ? signataire.getCivilite().getLibelle() : null;
                this.fonction = signataire.getFonction();
                this.mail = signataire.getMail();
                this.nom = signataire.getNom();
                this.prenom = signataire.getPrenom();
                this.tel = signataire.getTel();
            }
        }
    }

    @Data
    @NoArgsConstructor
    public static class StructureContext {
        private String activitePrincipale;
        private String codePostal;
        private String commune;
        private String effectifLibelle;
        private String mail;
        private String numeroSiret;
        private String paysLibelle;
        private String raisonSociale;
        private String statutJuridiqueLibelle;
        private String telephone;
        private String typeStructureLibelle;
        private String voie;
        private String batiment;

        public StructureContext(Structure structure) {
            this.activitePrincipale = structure.getActivitePrincipale();
            this.codePostal = structure.getCodePostal();
            this.commune = structure.getCommune();
            this.effectifLibelle = structure.getEffectif() != null ? structure.getEffectif().getLibelle() : null;
            this.mail = structure.getMail();
            this.numeroSiret = structure.getNumeroSiret();
            this.paysLibelle = structure.getPays() != null ? structure.getPays().getLib() : null;
            this.raisonSociale = structure.getRaisonSociale();
            this.statutJuridiqueLibelle = structure.getStatutJuridique() != null ? structure.getStatutJuridique().getLibelle() : null;
            this.telephone = structure.getTelephone();
            this.typeStructureLibelle = structure.getTypeStructure() != null ? structure.getTypeStructure().getLibelle() : null;
            this.voie = structure.getVoie();
            this.batiment = structure.getBatimentResidence();
        }
    }

    @Data
    @NoArgsConstructor
    public static class AvenantContext {
        private String id;
        private String sujetStage;
        private String motifAvenant;
        private String dateDebutStage;
        private String dateFinStage;
        private boolean rupture;
        private String dateRupture;
        private String commentaireRupture;
        private boolean modificationSujet;
        private boolean modificationPeriode;
        private List<PeriodeInterruptionContext> periodesInterruptions = new ArrayList<>();
        private boolean modificationMontantGratification;
        private String montantGratification;
        private String uniteGratificationLibelle;
        private String uniteDureeGratificationLibelle;
        private String deviseGratification;
        private String modeVersGratificationLibelle;
        private boolean modificationLieu;
        private ServiceContext service;
        private boolean modificationSalarie;
        private ContactContext contact;
        private boolean modificationEnseignant;
        private EnseignantContext enseignant;

        public AvenantContext(Avenant avenant) {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

            this.id = String.valueOf(avenant.getId());
            this.sujetStage = avenant.getSujetStage();
            this.motifAvenant = avenant.getMotifAvenant();
            this.dateDebutStage = avenant.getDateDebutStage() != null ? df.format(avenant.getDateDebutStage()) : "";
            this.dateFinStage = avenant.getDateFinStage() != null ? df.format(avenant.getDateFinStage()) : "";
            this.rupture = avenant.isRupture();
            this.dateRupture = avenant.getDateRupture() != null ? df.format(avenant.getDateRupture()) : "";
            this.commentaireRupture = avenant.getCommentaireRupture();
            this.modificationSujet = avenant.isModificationSujet();
            this.modificationPeriode = avenant.isModificationPeriode();
            for (PeriodeInterruptionAvenant periode : avenant.getPeriodeInterruptionAvenants()) {
                this.periodesInterruptions.add(new PeriodeInterruptionContext(periode.getDateDebutInterruption() != null ? df.format(periode.getDateDebutInterruption()) : null, periode.getDateFinInterruption() != null ? df.format(periode.getDateFinInterruption()) : null));
            }
            this.modificationMontantGratification = avenant.isModificationMontantGratification();
            if (this.modificationMontantGratification) {
                this.montantGratification = avenant.getMontantGratification();
                this.uniteGratificationLibelle = avenant.getUniteGratification() != null ? avenant.getUniteGratification().getLibelle() : null;
                this.uniteDureeGratificationLibelle = avenant.getUniteDuree() != null ? avenant.getUniteDuree().getLibelle() : null;
                this.deviseGratification = avenant.getDevise() != null ? avenant.getDevise().getLibelle() : null;
                this.modeVersGratificationLibelle = avenant.getModeVersGratification() != null ? avenant.getModeVersGratification().getLibelle() : null;
            }
            this.modificationLieu = avenant.getModificationLieu() != null && avenant.getModificationLieu();
            if (this.modificationLieu) {
                this.service = new ServiceContext(avenant.getService());
            }
            this.modificationSalarie = avenant.isModificationSalarie();
            if (this.modificationSalarie) {
                this.contact = new ContactContext(avenant.getContact());
            }
            this.modificationEnseignant = avenant.isModificationEnseignant();
            if (this.modificationEnseignant) {
                this.enseignant = new EnseignantContext(avenant.getEnseignant());
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodeInterruptionContext {
        private String dateDebutInterruption;
        private String dateFinInterruption;
    }

    public static class HoraireIrregulierContext {
        private String dateDebutPeriode;
        private String dateFinPeriode;
        private Integer nbHeuresJournalieres;

        public HoraireIrregulierContext() {

        }

        public HoraireIrregulierContext(String dateDebutPeriode, String dateFinPeriode, Integer nbHeuresJournalieres) {
            this.dateDebutPeriode = dateDebutPeriode;
            this.dateFinPeriode = dateFinPeriode;
            this.nbHeuresJournalieres = nbHeuresJournalieres;
        }

        public String getDateDebutPeriode() {
            return dateDebutPeriode != null ? dateDebutPeriode : "";
        }

        public void setDateDebutPeriode(String dateDebutPeriode) {
            this.dateDebutPeriode = dateDebutPeriode;
        }

        public String getDateFinPeriode() {
            return dateFinPeriode != null ? dateFinPeriode : "";
        }

        public void setDateFinPeriode(String dateFinPeriode) {
            this.dateFinPeriode = dateFinPeriode;
        }

        public Integer getNbHeuresJournalieres() {
            return this.nbHeuresJournalieres;
        }

        public void setNbHeuresJournalieres(Integer nbHeuresJournalieres) {
            this.nbHeuresJournalieres = nbHeuresJournalieres;
        }

    }

    @Data
    @NoArgsConstructor
    private static class ReponseEvaluationContext {
        private Integer reponseEnt1;
        @Lob
        private String reponseEnt1bis;
        private Integer reponseEnt2;
        @Lob
        private String reponseEnt2bis;
        private Integer reponseEnt3;
        private Integer reponseEnt4;
        @Lob
        private String reponseEnt4bis;
        private Integer reponseEnt5;
        @Lob
        private String reponseEnt5bis;
        private Integer reponseEnt6;
        @Lob
        private String reponseEnt6bis;
        private Integer reponseEnt7;
        @Lob
        private String reponseEnt7bis;
        private Integer reponseEnt8;
        @Lob
        private String reponseEnt8bis;
        private Integer reponseEnt9;
        @Lob
        private String reponseEnt9bis;
        private Boolean reponseEnt10;
        @Lob
        private String reponseEnt10bis;
        private Integer reponseEnt11;
        @Lob
        private String reponseEnt11bis;
        private Integer reponseEnt12;
        @Lob
        private String reponseEnt12bis;
        private Integer reponseEnt13;
        @Lob
        private String reponseEnt13bis;
        private Integer reponseEnt14;
        @Lob
        private String reponseEnt14bis;
        private Integer reponseEnt15;
        @Lob
        private String reponseEnt15bis;
        private Integer reponseEnt16;
        @Lob
        private String reponseEnt16bis;
        private Integer reponseEnt17;
        @Lob
        private String reponseEnt17bis;
        private Boolean reponseEnt18;
        @Lob
        private String reponseEnt18bis;
        @Lob
        private String reponseEnt19;
    }

    @Data
    @NoArgsConstructor
    private static class FicheEvaluationContext {
        private Boolean questionEnt1;
        private Boolean questionEnt2;
        private Boolean questionEnt3;
        private Boolean questionEnt4;
        private Boolean questionEnt5;
        private Boolean questionEnt6;
        private Boolean questionEnt7;
        private Boolean questionEnt8;
        private Boolean questionEnt9;
        private Boolean questionEnt10;
        private Boolean questionEnt11;
        private Boolean questionEnt12;
        private Boolean questionEnt13;
        private Boolean questionEnt14;
        private Boolean questionEnt15;
        private Boolean questionEnt16;
        private Boolean questionEnt17;
        private Boolean questionEnt18;
        private Boolean questionEnt19;
    }

    @Data
    @NoArgsConstructor
    private static class QuestionSupplementaireContext {
        private int id;
        private String question;
        private String typeQuestion;

        public QuestionSupplementaireContext(QuestionSupplementaire questionSupplementaire) {
            this.id = questionSupplementaire.getId();
            this.question = questionSupplementaire.getQuestion();
            this.typeQuestion = questionSupplementaire.getTypeQuestion();
        }
    }

    @Data
    @NoArgsConstructor
    private static class ReponseSupplementaireContext {
        private int idQuestionSupplementaire;
        private int idConvention;
        private String reponseTxt;
        private Integer reponseInt;
        private Boolean reponseBool;

        public ReponseSupplementaireContext(ReponseSupplementaire reponseSupplementaire) {
            this.idQuestionSupplementaire = reponseSupplementaire.getId().getIdQuestionSupplementaire();
            this.idConvention = reponseSupplementaire.getId().getIdConvention();
            this.reponseTxt = reponseSupplementaire.getReponseTxt();
            this.reponseInt = reponseSupplementaire.getReponseInt();
            this.reponseBool = reponseSupplementaire.getReponseBool();
        }
    }
}
