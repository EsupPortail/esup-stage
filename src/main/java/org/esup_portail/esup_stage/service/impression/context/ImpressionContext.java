package org.esup_portail.esup_stage.service.impression.context;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.esup_portail.esup_stage.enums.TypeQuestionEvaluation;
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
    private List<QuestionEvaluationContext> questionEvaluations = new ArrayList<>();
    private List<QuestionSupplementaireContext> questionsSupplementaires = new ArrayList<>();
    private List<ReponseSupplementaireContext> reponsesSupplementaires = new ArrayList<>();

    public ImpressionContext(Convention convention, Avenant avenant, CentreGestion centreEtablissement, List<QuestionSupplementaire> questionSupplementaires, List<QuestionEvaluation> questionEvaluations) {
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
            this.ficheEvaluation = new FicheEvaluationContext(ficheEvaluation);
            this.reponse = new ReponseEvaluationContext(convention.getReponseEvaluation());
            for (QuestionSupplementaire question : questionSupplementaires) {
                this.questionsSupplementaires.add(new QuestionSupplementaireContext(question));
            }
            for (ReponseSupplementaire reponse : convention.getReponseSupplementaires()) {
                this.reponsesSupplementaires.add(new ReponseSupplementaireContext(reponse));
            }
            for (QuestionEvaluation q : questionEvaluations) {
                this.questionEvaluations.add(new QuestionEvaluationContext(q));
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
        private String mailDelegataire;
        private String nomDelegataire;
        private String prenomDelegataire;
        private String qualiteDelegataire;

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
            this.mailDelegataire = centreGestion.getMailDelegataireViseur();
            this.nomDelegataire = centreGestion.getNomDelegataireViseur();
            this.prenomDelegataire = centreGestion.getPrenomDelegataireViseur();
            this.qualiteDelegataire = centreGestion.getQualiteDelegataireViseur();
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
    @NoArgsConstructor
    public static class ServiceContext {
        private String codePostal;
        private String commune;
        private String nom;
        private String paysLibelle;
        private String voie;
        private String batiment;

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoraireIrregulierContext {
        private String dateDebutPeriode;
        private String dateFinPeriode;
        private Integer nbHeuresJournalieres;
    }

    @Data
    @NoArgsConstructor
    public static class ReponseEvaluationContext {
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

        private Integer reponseEtuI1;
        @Lob
        private String reponseEtuI1bis;
        private Integer reponseEtuI2;
        private Integer reponseEtuI3;
        private Boolean reponseEtuI4a;
        private Boolean reponseEtuI4b;
        private Boolean reponseEtuI4c;
        private Boolean reponseEtuI4d;
        private Integer reponseEtuI5;
        private Integer reponseEtuI6;
        private Boolean reponseEtuI7;
        private Integer reponseEtuI7bis1;
        private Integer reponseEtuI7bis1a;
        private String reponseEtuI7bis1b;
        private Integer reponseEtuI7bis2;
        private Boolean reponseEtuI8;

        private Integer reponseEtuII1;
        @Lob
        private String reponseEtuII1bis;
        private Integer reponseEtuII2;
        @Lob
        private String reponseEtuII2bis;
        private Integer reponseEtuII3;
        @Lob
        private String reponseEtuII3bis;
        private Integer reponseEtuII4;
        private Boolean reponseEtuII5;
        private Integer reponseEtuII5a;
        private Boolean reponseEtuII5b;
        private Boolean reponseEtuII6;

        private Boolean reponseEtuIII1;
        @Lob
        private String reponseEtuIII1bis;
        private Boolean reponseEtuIII2;
        @Lob
        private String reponseEtuIII2bis;
        private Boolean reponseEtuIII3;
        @Lob
        private String reponseEtuIII3bis;
        private Integer reponseEtuIII4;
        private Boolean reponseEtuIII5a;
        private Boolean reponseEtuIII5b;
        private Boolean reponseEtuIII5c;
        @Lob
        private String reponseEtuIII5bis;
        private Integer reponseEtuIII6;
        @Lob
        private String reponseEtuIII6bis;
        private Integer reponseEtuIII7;
        @Lob
        private String reponseEtuIII7bis;
        private Boolean reponseEtuIII8;
        @Lob
        private String reponseEtuIII8bis;
        private Boolean reponseEtuIII9;
        @Lob
        private String reponseEtuIII9bis;
        private Boolean reponseEtuIII10;
        private Boolean reponseEtuIII11;
        private Boolean reponseEtuIII12;
        private Boolean reponseEtuIII13;
        private Boolean reponseEtuIII14;
        private Integer reponseEtuIII15;
        @Lob
        private String reponseEtuIII15bis;
        private Integer reponseEtuIII16;
        @Lob
        private String reponseEtuIII16bis;

        private Boolean reponseEnsI1a;
        private Boolean reponseEnsI1b;
        private Boolean reponseEnsI1c;
        private Boolean reponseEnsI2a;
        private Boolean reponseEnsI2b;
        private Boolean reponseEnsI2c;
        @Lob
        private String reponseEnsI3;

        private Integer reponseEnsII1;
        private Integer reponseEnsII2;
        private Integer reponseEnsII3;
        private Integer reponseEnsII4;
        private Integer reponseEnsII5;
        private Integer reponseEnsII6;
        private Integer reponseEnsII7;
        private Integer reponseEnsII8;
        private Integer reponseEnsII9;
        private Integer reponseEnsII10;
        @Lob
        private String reponseEnsII11;

        public ReponseEvaluationContext(ReponseEvaluation reponseEvaluation) {
            this.reponseEnt1 = reponseEvaluation.getReponseEnt1();
            this.reponseEnt1bis = reponseEvaluation.getReponseEnt1bis();
            this.reponseEnt2 = reponseEvaluation.getReponseEnt2();
            this.reponseEnt2bis = reponseEvaluation.getReponseEnt2bis();
            this.reponseEnt3 = reponseEvaluation.getReponseEnt3();
            this.reponseEnt4 = reponseEvaluation.getReponseEnt4();
            this.reponseEnt4bis = reponseEvaluation.getReponseEnt4bis();
            this.reponseEnt5 = reponseEvaluation.getReponseEnt5();
            this.reponseEnt5bis = reponseEvaluation.getReponseEnt5bis();
            this.reponseEnt6 = reponseEvaluation.getReponseEnt6();
            this.reponseEnt6bis = reponseEvaluation.getReponseEnt6bis();
            this.reponseEnt7 = reponseEvaluation.getReponseEnt7();
            this.reponseEnt7bis = reponseEvaluation.getReponseEnt7bis();
            this.reponseEnt8 = reponseEvaluation.getReponseEnt8();
            this.reponseEnt8bis = reponseEvaluation.getReponseEnt8bis();
            this.reponseEnt9 = reponseEvaluation.getReponseEnt9();
            this.reponseEnt9bis = reponseEvaluation.getReponseEnt9bis();
            this.reponseEnt10 = reponseEvaluation.getReponseEnt10();
            this.reponseEnt10bis = reponseEvaluation.getReponseEnt10bis();
            this.reponseEnt11 = reponseEvaluation.getReponseEnt11();
            this.reponseEnt11bis = reponseEvaluation.getReponseEnt11bis();
            this.reponseEnt12 = reponseEvaluation.getReponseEnt12();
            this.reponseEnt12bis = reponseEvaluation.getReponseEnt12bis();
            this.reponseEnt13 = reponseEvaluation.getReponseEnt13();
            this.reponseEnt13bis = reponseEvaluation.getReponseEnt13bis();
            this.reponseEnt14 = reponseEvaluation.getReponseEnt14();
            this.reponseEnt14bis = reponseEvaluation.getReponseEnt14bis();
            this.reponseEnt15 = reponseEvaluation.getReponseEnt15();
            this.reponseEnt15bis = reponseEvaluation.getReponseEnt15bis();
            this.reponseEnt16 = reponseEvaluation.getReponseEnt16();
            this.reponseEnt16bis = reponseEvaluation.getReponseEnt16bis();
            this.reponseEnt17 = reponseEvaluation.getReponseEnt17();
            this.reponseEnt17bis = reponseEvaluation.getReponseEnt17bis();
            this.reponseEnt18 = reponseEvaluation.getReponseEnt18();
            this.reponseEnt18bis = reponseEvaluation.getReponseEnt18bis();
            this.reponseEnt19 = reponseEvaluation.getReponseEnt19();

            this.reponseEtuI1 = reponseEvaluation.getReponseEtuI1();
            this.reponseEtuI1bis = reponseEvaluation.getReponseEtuI1bis();
            this.reponseEtuI2 = reponseEvaluation.getReponseEtuI2();
            this.reponseEtuI3 = reponseEvaluation.getReponseEtuI3();
            this.reponseEtuI4a = reponseEvaluation.getReponseEtuI4a();
            this.reponseEtuI4b = reponseEvaluation.getReponseEtuI4b();
            this.reponseEtuI4c = reponseEvaluation.getReponseEtuI4c();
            this.reponseEtuI4d = reponseEvaluation.getReponseEtuI4d();
            this.reponseEtuI5 = reponseEvaluation.getReponseEtuI5();
            this.reponseEtuI6 = reponseEvaluation.getReponseEtuI6();
            this.reponseEtuI7 = reponseEvaluation.getReponseEtuI7();
            this.reponseEtuI7bis1 = reponseEvaluation.getReponseEtuI7bis1();
            this.reponseEtuI7bis1a = reponseEvaluation.getReponseEtuI7bis1a();
            this.reponseEtuI7bis1b = reponseEvaluation.getReponseEtuI7bis1b();
            this.reponseEtuI7bis2 = reponseEvaluation.getReponseEtuI7bis2();
            this.reponseEtuI8 = reponseEvaluation.getReponseEtuI8();

            this.reponseEtuII1 = reponseEvaluation.getReponseEtuII1();
            this.reponseEtuII1bis = reponseEvaluation.getReponseEtuII1bis();
            this.reponseEtuII2 = reponseEvaluation.getReponseEtuII2();
            this.reponseEtuII2bis = reponseEvaluation.getReponseEtuII2bis();
            this.reponseEtuII3 = reponseEvaluation.getReponseEtuII3();
            this.reponseEtuII3bis = reponseEvaluation.getReponseEtuII3bis();
            this.reponseEtuII4 = reponseEvaluation.getReponseEtuII4();
            this.reponseEtuII5 = reponseEvaluation.getReponseEtuII5();
            this.reponseEtuII5a = reponseEvaluation.getReponseEtuII5a();
            this.reponseEtuII5b = reponseEvaluation.getReponseEtuII5b();
            this.reponseEtuII6 = reponseEvaluation.getReponseEtuII6();

            this.reponseEtuIII1 = reponseEvaluation.getReponseEtuIII1();
            this.reponseEtuIII1bis = reponseEvaluation.getReponseEtuIII1bis();
            this.reponseEtuIII2 = reponseEvaluation.getReponseEtuIII2();
            this.reponseEtuIII2bis = reponseEvaluation.getReponseEtuIII2bis();
            this.reponseEtuIII3 = reponseEvaluation.getReponseEtuIII3();
            this.reponseEtuIII3bis = reponseEvaluation.getReponseEtuIII3bis();
            this.reponseEtuIII4 = reponseEvaluation.getReponseEtuIII4();
            this.reponseEtuIII5a = reponseEvaluation.getReponseEtuIII5a();
            this.reponseEtuIII5b = reponseEvaluation.getReponseEtuIII5b();
            this.reponseEtuIII5c = reponseEvaluation.getReponseEtuIII5c();
            this.reponseEtuIII5bis = reponseEvaluation.getReponseEtuIII5bis();
            this.reponseEtuIII6 = reponseEvaluation.getReponseEtuIII6();
            this.reponseEtuIII6bis = reponseEvaluation.getReponseEtuIII6bis();
            this.reponseEtuIII7 = reponseEvaluation.getReponseEtuIII7();
            this.reponseEtuIII7bis = reponseEvaluation.getReponseEtuIII7bis();
            this.reponseEtuIII8 = reponseEvaluation.getReponseEtuIII8();
            this.reponseEtuIII8bis = reponseEvaluation.getReponseEtuIII8bis();
            this.reponseEtuIII9 = reponseEvaluation.getReponseEtuIII9();
            this.reponseEtuIII9bis = reponseEvaluation.getReponseEtuIII9bis();
            this.reponseEtuIII10 = reponseEvaluation.getReponseEtuIII10();
            this.reponseEtuIII11 = reponseEvaluation.getReponseEtuIII11();
            this.reponseEtuIII12 = reponseEvaluation.getReponseEtuIII12();
            this.reponseEtuIII13 = reponseEvaluation.getReponseEtuIII13();
            this.reponseEtuIII14 = reponseEvaluation.getReponseEtuIII14();
            this.reponseEtuIII15 = reponseEvaluation.getReponseEtuIII15();
            this.reponseEtuIII15bis = reponseEvaluation.getReponseEtuIII15bis();
            this.reponseEtuIII16 = reponseEvaluation.getReponseEtuIII16();
            this.reponseEtuIII16bis = reponseEvaluation.getReponseEtuIII16bis();

            this.reponseEnsI1a = reponseEvaluation.getReponseEnsI1a();
            this.reponseEnsI1b = reponseEvaluation.getReponseEnsI1b();
            this.reponseEnsI1c = reponseEvaluation.getReponseEnsI1c();
            this.reponseEnsI2a = reponseEvaluation.getReponseEnsI2a();
            this.reponseEnsI2b = reponseEvaluation.getReponseEnsI2b();
            this.reponseEnsI2c = reponseEvaluation.getReponseEnsI2c();
            this.reponseEnsI3 = reponseEvaluation.getReponseEnsI3();

            this.reponseEnsII1 = reponseEvaluation.getReponseEnsII1();
            this.reponseEnsII2 = reponseEvaluation.getReponseEnsII2();
            this.reponseEnsII3 = reponseEvaluation.getReponseEnsII3();
            this.reponseEnsII4 = reponseEvaluation.getReponseEnsII4();
            this.reponseEnsII5 = reponseEvaluation.getReponseEnsII5();
            this.reponseEnsII6 = reponseEvaluation.getReponseEnsII6();
            this.reponseEnsII7 = reponseEvaluation.getReponseEnsII7();
            this.reponseEnsII8 = reponseEvaluation.getReponseEnsII8();
            this.reponseEnsII9 = reponseEvaluation.getReponseEnsII9();
            this.reponseEnsII10 = reponseEvaluation.getReponseEnsII10();
            this.reponseEnsII11 = reponseEvaluation.getReponseEnsII11();
        }
    }

    @Data
    @NoArgsConstructor
    public static class FicheEvaluationContext {
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

        private Boolean questionEtuI1;
        private Boolean questionEtuI2;
        private Boolean questionEtuI3;
        private Boolean questionEtuI4;
        private Boolean questionEtuI5;
        private Boolean questionEtuI6;
        private Boolean questionEtuI7;
        private Boolean questionEtuI8;

        private Boolean questionEtuII1;
        private Boolean questionEtuII2;
        private Boolean questionEtuII3;
        private Boolean questionEtuII4;
        private Boolean questionEtuII5;
        private Boolean questionEtuII6;

        private Boolean questionEtuIII1;
        private Boolean questionEtuIII2;
        private Boolean questionEtuIII3;
        private Boolean questionEtuIII4;
        private Boolean questionEtuIII5;
        private Boolean questionEtuIII6;
        private Boolean questionEtuIII7;
        private Boolean questionEtuIII8;
        private Boolean questionEtuIII9;
        private Boolean questionEtuIII10;
        private Boolean questionEtuIII11;
        private Boolean questionEtuIII12;
        private Boolean questionEtuIII13;
        private Boolean questionEtuIII14;
        private Boolean questionEtuIII15;
        private Boolean questionEtuIII16;

        private Boolean questionEnsI1;
        private Boolean questionEnsI2;
        private Boolean questionEnsI3;

        private Boolean questionEnsII1;
        private Boolean questionEnsII2;
        private Boolean questionEnsII3;
        private Boolean questionEnsII4;
        private Boolean questionEnsII5;
        private Boolean questionEnsII6;
        private Boolean questionEnsII7;
        private Boolean questionEnsII8;
        private Boolean questionEnsII9;
        private Boolean questionEnsII10;
        private Boolean questionEnsII11;

        public FicheEvaluationContext(FicheEvaluation ficheEvaluation){
            this.questionEnt1 = ficheEvaluation.getQuestionEnt1();
            this.questionEnt2 = ficheEvaluation.getQuestionEnt2();
            this.questionEnt3 = ficheEvaluation.getQuestionEnt3();
            this.questionEnt4 = ficheEvaluation.getQuestionEnt4();
            this.questionEnt5 = ficheEvaluation.getQuestionEnt5();
            this.questionEnt6 = ficheEvaluation.getQuestionEnt6();
            this.questionEnt7 = ficheEvaluation.getQuestionEnt7();
            this.questionEnt8 = ficheEvaluation.getQuestionEnt8();
            this.questionEnt9 = ficheEvaluation.getQuestionEnt9();
            this.questionEnt10 = ficheEvaluation.getQuestionEnt10();
            this.questionEnt11 = ficheEvaluation.getQuestionEnt11();
            this.questionEnt12 = ficheEvaluation.getQuestionEnt12();
            this.questionEnt13 = ficheEvaluation.getQuestionEnt13();
            this.questionEnt14 = ficheEvaluation.getQuestionEnt14();
            this.questionEnt15 = ficheEvaluation.getQuestionEnt15();
            this.questionEnt16 = ficheEvaluation.getQuestionEnt16();
            this.questionEnt17 = ficheEvaluation.getQuestionEnt17();
            this.questionEnt18 = ficheEvaluation.getQuestionEnt18();
            this.questionEnt19 = ficheEvaluation.getQuestionEnt19();

            this.questionEtuI1 = ficheEvaluation.getQuestionEtuI1();
            this.questionEtuI2 = ficheEvaluation.getQuestionEtuI2();
            this.questionEtuI3 = ficheEvaluation.getQuestionEtuI3();
            this.questionEtuI4 = ficheEvaluation.getQuestionEtuI4();
            this.questionEtuI5 = ficheEvaluation.getQuestionEtuI5();
            this.questionEtuI6 = ficheEvaluation.getQuestionEtuI6();
            this.questionEtuI7 = ficheEvaluation.getQuestionEtuI7();
            this.questionEtuI8 = ficheEvaluation.getQuestionEtuI8();

            this.questionEtuII1 = ficheEvaluation.getQuestionEtuII1();
            this.questionEtuII2 = ficheEvaluation.getQuestionEtuII2();
            this.questionEtuII3 = ficheEvaluation.getQuestionEtuII3();
            this.questionEtuII4 = ficheEvaluation.getQuestionEtuII4();
            this.questionEtuII5 = ficheEvaluation.getQuestionEtuII5();
            this.questionEtuII6 = ficheEvaluation.getQuestionEtuII6();

            this.questionEtuIII1 = ficheEvaluation.getQuestionEtuIII1();
            this.questionEtuIII2 = ficheEvaluation.getQuestionEtuIII2();
            this.questionEtuIII3 = ficheEvaluation.getQuestionEtuIII3();
            this.questionEtuIII4 = ficheEvaluation.getQuestionEtuIII4();
            this.questionEtuIII5 = ficheEvaluation.getQuestionEtuIII5();
            this.questionEtuIII6 = ficheEvaluation.getQuestionEtuIII6();
            this.questionEtuIII7 = ficheEvaluation.getQuestionEtuIII7();
            this.questionEtuIII8 = ficheEvaluation.getQuestionEtuIII8();
            this.questionEtuIII9 = ficheEvaluation.getQuestionEtuIII9();
            this.questionEtuIII10 = ficheEvaluation.getQuestionEtuIII10();
            this.questionEtuIII11 = ficheEvaluation.getQuestionEtuIII11();
            this.questionEtuIII12 = ficheEvaluation.getQuestionEtuIII12();
            this.questionEtuIII13 = ficheEvaluation.getQuestionEtuIII13();
            this.questionEtuIII14 = ficheEvaluation.getQuestionEtuIII14();
            this.questionEtuIII15 = ficheEvaluation.getQuestionEtuIII15();
            this.questionEtuIII16 = ficheEvaluation.getQuestionEtuIII16();

            this.questionEnsI1 = ficheEvaluation.getQuestionEnsI1();
            this.questionEnsI2 = ficheEvaluation.getQuestionEnsI2();
            this.questionEnsI3 = ficheEvaluation.getQuestionEnsI3();

            this.questionEnsII1 = ficheEvaluation.getQuestionEnsII1();
            this.questionEnsII2 = ficheEvaluation.getQuestionEnsII2();
            this.questionEnsII3 = ficheEvaluation.getQuestionEnsII3();
            this.questionEnsII4 = ficheEvaluation.getQuestionEnsII4();
            this.questionEnsII5 = ficheEvaluation.getQuestionEnsII5();
            this.questionEnsII6 = ficheEvaluation.getQuestionEnsII6();
            this.questionEnsII7 = ficheEvaluation.getQuestionEnsII7();
            this.questionEnsII8 = ficheEvaluation.getQuestionEnsII8();
            this.questionEnsII9 = ficheEvaluation.getQuestionEnsII9();
            this.questionEnsII10 = ficheEvaluation.getQuestionEnsII10();
            this.questionEnsII11 = ficheEvaluation.getQuestionEnsII11();
        }
    }

    @Data
    @NoArgsConstructor
    public static class QuestionSupplementaireContext {
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
    public static class ReponseSupplementaireContext {
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

    @Data
    @NoArgsConstructor
    public static class QuestionEvaluationContext {
        private String code;
        private String texte;
        private TypeQuestionEvaluation type;

        public QuestionEvaluationContext(QuestionEvaluation questionEvaluation) {
            this.code = questionEvaluation.getCode();
            this.texte = questionEvaluation.getTexte();
            this.type = questionEvaluation.getType();
        }
    }
}
