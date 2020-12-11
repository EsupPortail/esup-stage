package fr.esupportail.esupstage.domain.jpa.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * The persistent class for the CentreGestion database table.
 *
 */
@Entity
@Table(name = "CentreGestion")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "CentreGestion.findAll", query = "SELECT c FROM CentreGestion c")
public class CentreGestion implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idCentreGestion;
    @Column(length = 200)
    private String adresse;
    @Column(nullable = false)
    private boolean autorisationEtudiantCreationConvention;
    @Column(nullable = false)
    private boolean autoriserImpressionConvention;
    private boolean choixAnneeApresDebutAnnee;
    private boolean choixAnneeAvantDebutAnnee;
    @Column(length = 10)
    private String codePostal;
    @Column(nullable = false, length = 50)
    private String codeUniversite;
    @Lob
    private String commentaire;
    @Column(length = 200)
    private String commune;
    @Column(nullable = false)
    private Date dateCreation;
    private Date dateModif;
    @Column(nullable = false)
    private boolean depotAnonyme;
    @Column(length = 20)
    private String fax;
    @Column(nullable = false)
    private Integer idModeValidationStage;
    @Column(nullable = false, length = 50)
    private String loginCreation;
    @Column(length = 50)
    private String loginModif;
    @Column(length = 50)
    private String mail;
    @Column(length = 100)
    private String nomCentre;
    @Column(length = 50)
    private String nomViseur;
    @Column(length = 50)
    private String prenomViseur;
    @Column(nullable = false)
    private boolean presenceTuteurEns;
    @Column(nullable = false)
    private boolean presenceTuteurPro;
    @Column(length = 100)
    private String qualiteViseur;
    @Column(nullable = false)
    private boolean saisieTuteurProParEtudiant;
    @Column(length = 50)
    private String siteWeb;
    @Column(length = 20)
    private String telephone;
    @Column(length = 200)
    private String urlPageInstruction;
    private boolean validationPedagogique;
    private boolean visibiliteEvalPro;
    @Column(length = 200)
    private String voie;
    // bi-directional many-to-one association to CentreGestionSuperViseur
    @ManyToOne
    @JoinColumn(name = "idCentreGestionSuperViseur")
    private CentreGestionSuperViseur centreGestionSuperViseur;
    // bi-directional many-to-one association to Confidentialite
    @ManyToOne
    @JoinColumn(name = "codeConfidentialite", nullable = false)
    private Confidentialite confidentialite;
    // bi-directional many-to-one association to Fichier
    @ManyToOne
    @JoinColumn(name = "idFichier")
    private Fichier fichier;
    // bi-directional many-to-one association to NiveauCentre
    @ManyToOne
    @JoinColumn(name = "idNiveauCentre", nullable = false)
    private NiveauCentre niveauCentre;
    // bi-directional many-to-one association to Contact
    @OneToMany(mappedBy = "centreGestion")
    private List<Contact> contacts;
    // bi-directional many-to-one association to Convention
    @OneToMany(mappedBy = "centreGestion")
    private List<Convention> conventions;
    // bi-directional many-to-one association to CritereGestion
    @OneToMany(mappedBy = "centreGestion")
    private List<CritereGestion> critereGestions;
    // bi-directional many-to-one association to FicheEvaluation
    @OneToMany(mappedBy = "centreGestion")
    private List<FicheEvaluation> ficheEvaluations;
    // bi-directional many-to-one association to Offre
    @OneToMany(mappedBy = "centreGestion")
    private List<Offre> offres;
    // bi-directional many-to-one association to OffreDiffusion
    @OneToMany(mappedBy = "centreGestion")
    private List<OffreDiffusion> offreDiffusions;
    // bi-directional many-to-one association to PersonnelCentreGestion
    @OneToMany(mappedBy = "centreGestion")
    private List<PersonnelCentreGestion> personnelCentreGestions;

    public Convention addConvention(Convention convention) {
        getConventions().add(convention);
        convention.setCentreGestion(this);
        return convention;
    }

    public Convention removeConvention(Convention convention) {
        getConventions().remove(convention);
        convention.setCentreGestion(null);
        return convention;
    }

    public CritereGestion addCritereGestion(CritereGestion critereGestion) {
        getCritereGestions().add(critereGestion);
        critereGestion.setCentreGestion(this);
        return critereGestion;
    }

    public CritereGestion removeCritereGestion(CritereGestion critereGestion) {
        getCritereGestions().remove(critereGestion);
        critereGestion.setCentreGestion(null);
        return critereGestion;
    }

    public FicheEvaluation addFicheEvaluation(FicheEvaluation ficheEvaluation) {
        getFicheEvaluations().add(ficheEvaluation);
        ficheEvaluation.setCentreGestion(this);
        return ficheEvaluation;
    }

    public FicheEvaluation removeFicheEvaluation(FicheEvaluation ficheEvaluation) {
        getFicheEvaluations().remove(ficheEvaluation);
        ficheEvaluation.setCentreGestion(null);
        return ficheEvaluation;
    }

    public Offre addOffre(Offre offre) {
        getOffres().add(offre);
        offre.setCentreGestion(this);
        return offre;
    }

    public Offre removeOffre(Offre offre) {
        getOffres().remove(offre);
        offre.setCentreGestion(null);
        return offre;
    }


    public OffreDiffusion addOffreDiffusion(OffreDiffusion offreDiffusion) {
        getOffreDiffusions().add(offreDiffusion);
        offreDiffusion.setCentreGestion(this);
        return offreDiffusion;
    }

    public OffreDiffusion removeOffreDiffusion(OffreDiffusion offreDiffusion) {
        getOffreDiffusions().remove(offreDiffusion);
        offreDiffusion.setCentreGestion(null);
        return offreDiffusion;
    }

    public PersonnelCentreGestion addPersonnelCentreGestion(PersonnelCentreGestion personnelCentreGestion) {
        getPersonnelCentreGestions().add(personnelCentreGestion);
        personnelCentreGestion.setCentreGestion(this);
        return personnelCentreGestion;
    }

    public PersonnelCentreGestion removePersonnelCentreGestion(PersonnelCentreGestion personnelCentreGestion) {
        getPersonnelCentreGestions().remove(personnelCentreGestion);
        personnelCentreGestion.setCentreGestion(null);
        return personnelCentreGestion;
    }
}