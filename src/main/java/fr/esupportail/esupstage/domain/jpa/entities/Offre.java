package fr.esupportail.esupstage.domain.jpa.entities;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the Offre database table.
 *
 */
@Entity
@Table(name = "Offre")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "Offre.findAll", query = "SELECT o FROM Offre o")
public class Offre extends Auditable<String> {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idOffre;
    @Column(length = 50)
    private String anneeDebut;
    @Column(nullable = false, length = 50)
    private String anneeUniversitaire;
    @Column(length = 200)
    private String avantages;
    @Column(nullable = false)
    private boolean avecFichier;
    @Column(nullable = false)
    private boolean avecLien;
    @Column(nullable = false)
    private boolean cacherEtablissement;
    @Column(nullable = false)
    private boolean cacherFaxContactCand;
    @Column(nullable = false)
    private boolean cacherFaxContactInfo;
    @Column(nullable = false)
    private boolean cacherMailContactCand;
    @Column(nullable = false)
    private boolean cacherMailContactInfo;
    @Column(nullable = false)
    private boolean cacherNomContactCand;
    @Column(nullable = false)
    private boolean cacherNomContactInfo;
    @Column(nullable = false)
    private boolean cacherTelContactCand;
    @Column(nullable = false)
    private boolean cacherTelContactInfo;
    @Column(length = 10)
    private String codeCommune;
    @Column(length = 200)
    private String commentaireTempsTravail;
    @Lob
    private String competences;
    private LocalDate dateDiffusion;
    private LocalDate dateFinDiffusion;
    private LocalDateTime dateStopDiffusion;
    private LocalDateTime dateStopValidation;
    private LocalDateTime dateValidation;
    @Column(nullable = false)
    private boolean deplacement;
    @Lob
    @Column(nullable = false)
    private String description;
    private Integer duree;
    @Column(nullable = false)
    private boolean estAccessERQTH;
    @Column(nullable = false)
    private boolean estDiffusee;
    @Column(nullable = false)
    private boolean estPourvue;
    @Column(nullable = false)
    private boolean estPrioERQTH;
    @Column(nullable = false)
    private boolean estSupprimee;
    @Column(nullable = false)
    private boolean estValidee;
    @Column(nullable = false)
    private Integer etatValidation;
    @Column(nullable = false, length = 200)
    private String intitule;
    @Column(length = 200)
    private String lienAttache;
    @Column(length = 10)
    private String lieuCodePostal;
    @Column(length = 200)
    private String lieuCommune;
    @Column(length = 50)
    private String loginDiffusion;
    @Column(length = 50)
    private String loginRejetValidation;
    @Column(length = 50)
    private String loginStopDiffusion;
    @Column(length = 50)
    private String loginStopValidation;
    @Column(length = 50)
    private String loginValidation;
    @Column(length = 50)
    private String moisDebut;
    @Lob
    private String observations;
    @Column(nullable = false)
    private boolean offrePourvueEtudiantLocal;
    @Column(nullable = false)
    private boolean permis;
    @Column(length = 200)
    private String precisionDebut;
    @Column(length = 200)
    private String precisionHandicap;
    @Column(length = 200)
    private String precisionRemuneration;
    private Integer quotiteTravail;
    @Column(length = 100)
    private String referenceOffreEtablissement;
    @Column(nullable = false)
    private boolean remuneration;
    @Column(nullable = false)
    private boolean voiture;
    // bi-directional many-to-one association to Convention
    @OneToMany(mappedBy = "offre")
    private List<Convention> conventions;
    // bi-directional many-to-one association to CentreGestion
    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    private CentreGestion centreGestion;
    // bi-directional many-to-one association to Contact
    @ManyToOne
    @JoinColumn(name = "idContactCand")
    private Contact contact1;
    // bi-directional many-to-one association to Contact
    @ManyToOne
    @JoinColumn(name = "idContactInfo")
    private Contact contact2;
    // bi-directional many-to-one association to Contact
    @ManyToOne
    @JoinColumn(name = "idContactProprio")
    private Contact contact3;
    // bi-directional many-to-one association to ContratOffre
    @ManyToOne
    @JoinColumn(name = "idContratOffre")
    private ContratOffre contratOffre;
    // bi-directional many-to-one association to FapN1
    @ManyToOne
    @JoinColumn(name = "codeFAP_N3")
    private FapN1 fapN1;
    // bi-directional many-to-one association to Fichier
    @ManyToOne
    @JoinColumn(name = "idFichier")
    private Fichier fichier;
    // bi-directional many-to-one association to NiveauFormation
    @ManyToOne
    @JoinColumn(name = "idNiveauFormation")
    private NiveauFormation niveauFormation;
    // bi-directional many-to-one association to Pay
    @ManyToOne
    @JoinColumn(name = "idLieuPays")
    private Pays pay;
    // bi-directional many-to-one association to PersonnelCentreGestion
    @ManyToOne
    @JoinColumn(name = "idReferent")
    private PersonnelCentreGestion personnelCentreGestion;
    // bi-directional many-to-one association to FAP_QualificationSimplifiee
    @ManyToOne
    @JoinColumn(name = "idQualificationSimplifiee")
    private FAP_QualificationSimplifiee fapQualificationSimplifiee;
    // bi-directional many-to-one association to Structure
    @ManyToOne
    @JoinColumn(name = "idStructure", nullable = false)
    private Structure structure;
    // bi-directional many-to-one association to TempsTravail
    @ManyToOne
    @JoinColumn(name = "idTempsTravail")
    private TempsTravail tempsTravail;
    // bi-directional many-to-one association to TypeOffre
    @ManyToOne
    @JoinColumn(name = "idTypeOffre", nullable = false)
    private TypeOffre typeOffre;
    // bi-directional many-to-one association to UniteDuree
    @ManyToOne
    @JoinColumn(name = "idUniteDuree")
    private UniteDuree uniteDuree;
    // bi-directional many-to-one association to OffreDiffusion
    @OneToMany(mappedBy = "offre")
    private List<OffreDiffusion> offreDiffusions;
    // bi-directional many-to-many association to ModeCandidature
    @ManyToMany
    @JoinTable(name = "OffreModeCandidature", joinColumns = {
            @JoinColumn(name = "idOffre", nullable = false) }, inverseJoinColumns = {
            @JoinColumn(name = "idModeCandidature", nullable = false) })
    private List<ModeCandidature> modeCandidatures;

    public Convention addConvention(Convention convention) {
        getConventions().add(convention);
        convention.setOffre(this);
        return convention;
    }

    public Convention removeConvention(Convention convention) {
        getConventions().remove(convention);
        convention.setOffre(null);
        return convention;
    }

    public OffreDiffusion addOffreDiffusion(OffreDiffusion offreDiffusion) {
        getOffreDiffusions().add(offreDiffusion);
        offreDiffusion.setOffre(this);
        return offreDiffusion;
    }

    public OffreDiffusion removeOffreDiffusion(OffreDiffusion offreDiffusion) {
        getOffreDiffusions().remove(offreDiffusion);
        offreDiffusion.setOffre(null);
        return offreDiffusion;
    }
}