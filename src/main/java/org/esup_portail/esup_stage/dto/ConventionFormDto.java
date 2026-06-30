package org.esup_portail.esup_stage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.esup_portail.esup_stage.enums.NbJoursHebdoEnum;
import org.esup_portail.esup_stage.model.Pays;

import java.math.BigDecimal;
import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private String codeVersionEtape;

    @NotNull
    @NotEmpty
    private String libelleEtape;

    @NotNull
    @NotEmpty
    private String codeComposante;

    private String libelleComposante;

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
    @Size(max = 255)
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

    @Size(max = 5)
    private String nbHeuresHebdo;

    private String modeEncadreSuivi;

    private int idModeVersGratification;

    private String avantagesNature;

    private int idNatureTravail;

    private int idModeValidationStage;

    @Size(max = 255)
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

    @Size(max = 255)
    private String libelleCPAM;

    @Size(max = 255)
    private String regionCPAM;

    @Size(max = 255)
    private String adresseCPAM;

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

    private String inscriptionElp;

    private boolean horairesReguliers;

    private boolean gratificationStage;

    private boolean confidentiel;

    private Boolean protectionSocialeOrganismeAccueil;

}
