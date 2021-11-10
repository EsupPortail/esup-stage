package org.esup_portail.esup_stage.dto;

import org.esup_portail.esup_stage.enums.NbJoursHebdoEnum;
import org.esup_portail.esup_stage.model.Pays;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

public class ConventionFormDto {

    @NotNull
    private int idEtudiant;

    @NotNull
    private int idCentreGestion;

    @NotNull
    private int codeUFR;

    @NotNull
    private int codeEtape;

    @Size(max = 10)
    private String codeDepartement;

    @NotNull
    private int idEnseignant;

    @NotNull
    private int idStructure;

    @NotNull
    private int idService;

    @NotNull
    private int idContact;

    @NotNull
    private int idSignataire;

    @NotNull
    private int idTypeConvention;

    @NotNull
    private int idOffre;

    @NotNull
    @NotEmpty
    private String sujetStage;

    @NotNull
    @NotEmpty
    private Date dateDebutStage;

    @NotNull
    @NotEmpty
    private Date dateFinStage;

    @NotNull
    private Boolean interruptionStage;

    private Date dateDebutInterruption;

    private Date dateFinInterruption;

    @NotNull
    private NbJoursHebdoEnum nbJoursHebdo;

    @NotNull
    private int idTempsTravail;

    private String commentaireDureeTravail;

    @NotNull
    private int codeLangueConvention;

    @NotNull
    private int idOrigineStage;

    @NotNull
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

    @Size(max = 100)
    private String courrielPersoEtudiant;

    @Size(max = 20)
    private String telEtudiant;

    @Size(max = 20)
    private String telPortableEtudiant;

    @NotNull
    private int idIndemnisation;

    @Size(max = 15)
    private String montantGratification;

    private String fonctionsEtTaches;

    private String details;

    @Size(max = 10)
    private String annee;

    @NotNull
    private int idAssurance;

    @Size(max = 15)
    private String insee;

    @Size(max = 5)
    private String codeCaisse;

    @Size(max = 5)
    private String nbHeuresHebdo;

    private Integer quotiteTravail;

    private String modeEncadreSuivi;

    @NotNull
    private int idModeVersGratification;

    private String avantagesNature;

    @NotNull
    private int idNatureTravail;

    @NotNull
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

    @NotNull
    private int idUniteDureeExceptionnelle;

    @NotNull
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

    @NotNull
    private int idUniteDureeGratification;

    @Size(max = 50)
    private String monnaieGratification;

    @Size(max = 10)
    private String volumeHoraireFormation;

    @Size(max = 30)
    private String typePresence;

    @NotNull
    private int idDevise;

    @NotNull
    private Pays paysConvention;

    @NotNull
    private boolean horairesReguliers = false;

}
