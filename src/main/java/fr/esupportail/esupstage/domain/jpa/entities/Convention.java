package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * The persistent class for the Convention database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Convention")
@NamedQuery(name = "Convention.findAll", query = "SELECT c FROM Convention c")
public class Convention implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    @Column(unique = true, nullable = false)
    private Integer idConvention;

    @Column(length = 250)
    private String adresseEtabRef;

    @Column(length = 200)
    private String adresseEtudiant;

    @Column(length = 10)
    private String annee;

    @Lob
    private String avantagesNature;

    @Column(length = 5)
    private String codeCaisse;

    @Column(length = 1)
    private String codeCursusLMD;

    @Column(length = 10)
    private String codeDepartement;

    @Column(length = 8)
    private String codeElp;

    @Column(length = 3)
    private String codeFinalite;

    @Column(length = 10)

    private String codePostalEtudiant;

    @Column(length = 1)
    private String codeRGI;

    @Lob
    private String commentaireDureeTravail;

    @Lob
    private String commentaireStage;

    @Lob
    private String competences;

    private boolean conventionStructure;

    @Column(nullable = false)
    private boolean conversionEnContrat;

    @Column(length = 100)
    private String courrielPersoEtudiant;

    @Column(precision = 10, scale = 2)
    private BigDecimal creditECTS;

    @Column(nullable = false)
    private Date dateCreation;
    private Date dateDebutInterruption;
    @Column(nullable = false)
    private Date dateDebutStage;
    private Date dateEnvoiMailEtudiant;
    private Date dateEnvoiMailTuteurPedago;
    private Date dateEnvoiMailTuteurPro;
    private Date dateFinInterruption;
    @Column(nullable = false)
    private Date dateFinStage;
    private Date dateModif;
    private Date dateSignature;
    private Date dateValidation;
    @Lob
    private String details;
    @Column(length = 4)
    private String dureeExceptionnelle;
    @Column(nullable = false)
    private Integer dureeStage;
    private boolean envoiMailEtudiant;
    private boolean envoiMailTuteurPedago;
    private boolean envoiMailTuteurPro;
    @Lob
    private String fonctionsEtTaches;
    @Column(nullable = false)
    private Integer idAssurance;
    @Column(nullable = false)
    private Integer idModeVersGratification;
    private Integer idUniteDureeGratification;
    @Column(length = 15)
    private String insee;
    @Column(nullable = false)
    private boolean interruptionStage;
    @Column(length = 100)
    private String libelleCPAM;
    @Column(length = 60)
    private String libelleELP;
    @Column(length = 60)
    private String libelleFinalite;
    @Column(nullable = false, length = 50)
    private String loginCreation;
    @Column(length = 50)
    private String loginModif;
    @Column(length = 50)
    private String loginSignature;
    @Column(length = 50)
    private String loginValidation;
    @Lob
    private String modeEncadreSuivi;
    @Column(length = 50)
    private String monnaieGratification;
    @Column(length = 7)
    private String montantGratification;
    @Lob
    private String nbConges;
    @Column(length = 5)
    private String nbHeuresHebdo;
    @Column(nullable = false, length = 1)
    private String nbJoursHebdo;
    @Column(length = 100)
    private String nomEtabRef;
    @Column(length = 30)
    private String nomSignataireComposante;
    @Column(length = 50)
    private String paysEtudiant;
    private boolean priseEnChargeFraisMission;
    @Column(length = 60)
    private String qualiteSignataire;
    private Integer quotiteTravail;
    @Lob
    @Column(nullable = false)
    private String sujetStage;
    @Column(length = 20)
    private String telEtudiant;
    @Column(length = 20)
    private String telPortableEtudiant;
    @Column(nullable = false, length = 1)
    private String temConfSujetTeme;
    @Lob
    private String travailNuitFerie;
    private boolean validationConvention;
    private boolean validationPedagogique;
    @Column(length = 80)
    private String villeEtudiant;

    // bi-directional many-to-one association to Avenant
    @OneToMany(mappedBy = "convention")
    private List<Avenant> avenants;
    // bi-directional many-to-one association to CentreGestion
    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    private CentreGestion centreGestion;
    // bi-directional many-to-one association to Contact
    @ManyToOne
    @JoinColumn(name = "idContact")
    private Contact contact1;
    // bi-directional many-to-one association to Enseignant
    @ManyToOne
    @JoinColumn(name = "idEnseignant")
    private Enseignant enseignant;
    // bi-directional many-to-one association to Etape
    @ManyToOne
    @JoinColumns({ @JoinColumn(name = "codeEtape", referencedColumnName = "codeEtape"),
            @JoinColumn(name = "codeUniversiteEtape", referencedColumnName = "codeUniversite"),
            @JoinColumn(name = "codeVersionEtape", referencedColumnName = "codeVersionEtape") })
    private Etape etape;
    // bi-directional many-to-one association to Etudiant
    @ManyToOne
    @JoinColumn(name = "idEtudiant", nullable = false)
    private Etudiant etudiant;
    // bi-directional many-to-one association to Indemnisation
    @ManyToOne
    @JoinColumn(name = "idIndemnisation", nullable = false)
    private Indemnisation indemnisation;
    // bi-directional many-to-one association to LangueConvention
    @ManyToOne
    @JoinColumn(name = "codeLangueConvention", nullable = false)
    private LangueConvention langueConvention;
    // bi-directional many-to-one association to ModeValidationStage
    @ManyToOne
    @JoinColumn(name = "idModeValidationStage", nullable = false)
    private ModeValidationStage modeValidationStage;
    // bi-directional many-to-one association to NatureTravail
    @ManyToOne
    @JoinColumn(name = "idNatureTravail", nullable = false)
    private NatureTravail natureTravail;
    // bi-directional many-to-one association to Offre
    @ManyToOne
    @JoinColumn(name = "idOffre")
    private Offre offre;
    // bi-directional many-to-one association to OrigineStage
    @ManyToOne
    @JoinColumn(name = "idOrigineStage")
    private OrigineStage origineStage;
    // bi-directional many-to-one association to Service
    @ManyToOne
    @JoinColumn(name = "idService")
    private Service service;
    // bi-directional many-to-one association to Contact
    @ManyToOne
    @JoinColumn(name = "idSignataire")
    private Contact contact2;
    // bi-directional many-to-one association to Structure
    @ManyToOne
    @JoinColumn(name = "idStructure")
    private Structure structure;
    // bi-directional many-to-one association to TempsTravail
    @ManyToOne
    @JoinColumn(name = "idTempsTravail", nullable = false)
    private TempsTravail tempsTravail;
    // bi-directional many-to-one association to Theme
    @ManyToOne
    @JoinColumn(name = "idTheme", nullable = false)
    private Theme theme;
    // bi-directional many-to-one association to TypeConvention
    @ManyToOne
    @JoinColumn(name = "idTypeConvention", nullable = false)
    private TypeConvention typeConvention;
    // bi-directional many-to-one association to Ufr
    @ManyToOne
    @JoinColumns({ @JoinColumn(name = "codeUFR", referencedColumnName = "codeUFR"),
            @JoinColumn(name = "codeUniversiteUFR", referencedColumnName = "codeUniversite") })
    private Ufr ufr;
    // bi-directional many-to-one association to UniteDuree
    @ManyToOne
    @JoinColumn(name = "idUniteDureeExceptionnelle")
    private UniteDuree uniteDuree;
    // bi-directional many-to-one association to UniteGratification
    @ManyToOne
    @JoinColumn(name = "idUniteGratification")
    private UniteGratification uniteGratification;
    // bi-directional many-to-one association to ReponseEvaluation
    @OneToMany(mappedBy = "convention")
    private List<ReponseEvaluation> reponseEvaluations;
    // bi-directional many-to-one association to ReponseSupplementaire
    @OneToMany(mappedBy = "convention")
    private List<ReponseSupplementaire> reponseSupplementaires;

    public ReponseEvaluation addReponseEvaluation(ReponseEvaluation reponseEvaluation) {
        getReponseEvaluations().add(reponseEvaluation);
        reponseEvaluation.setConvention(this);
        return reponseEvaluation;
    }

    public ReponseEvaluation removeReponseEvaluation(ReponseEvaluation reponseEvaluation) {
        getReponseEvaluations().remove(reponseEvaluation);
        reponseEvaluation.setConvention(null);
        return reponseEvaluation;
    }

    public ReponseSupplementaire addReponseSupplementaire(ReponseSupplementaire reponseSupplementaire) {
        getReponseSupplementaires().add(reponseSupplementaire);
        reponseSupplementaire.setConvention(this);
        return reponseSupplementaire;
    }

    public ReponseSupplementaire removeReponseSupplementaire(ReponseSupplementaire reponseSupplementaire) {
        getReponseSupplementaires().remove(reponseSupplementaire);
        reponseSupplementaire.setConvention(null);
        return reponseSupplementaire;
    }
}