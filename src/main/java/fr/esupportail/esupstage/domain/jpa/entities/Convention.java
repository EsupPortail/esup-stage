package fr.esupportail.esupstage.domain.jpa.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the Convention database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Convention")
public class Convention extends Auditable<String> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idConvention")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

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

	@Column(columnDefinition = "decimal", precision = 7, scale = 2)
	private BigDecimal creditECTS;

	private LocalDate dateDebutInterruption;

	@Column(nullable = false)
	private LocalDate dateDebutStage;

	private LocalDateTime dateEnvoiMailEtudiant;

	private LocalDateTime dateEnvoiMailTuteurPedago;

	private LocalDateTime dateEnvoiMailTuteurPro;

	private LocalDate dateFinInterruption;

	@Column(nullable = false)
	private LocalDate dateFinStage;

	private LocalDateTime dateSignature;

	private LocalDateTime dateValidation;

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

	@Column(nullable = false, length = 3)
	private NbJourHebdo nbJoursHebdo;

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

	@OneToMany(mappedBy = "convention")
	private List<Avenant> avenants;

	@ManyToOne
	@JoinColumn(name = "idCentreGestion", nullable = false)
	private CentreGestion centreGestion;

	@ManyToOne
	@JoinColumn(name = "idContact")
	private Contact contact1;

	@ManyToOne
	@JoinColumn(name = "idEnseignant")
	private Enseignant enseignant;

	@ManyToOne
	// @formatter:off
	@JoinColumns({
		@JoinColumn(name = "codeEtape", referencedColumnName = "codeEtape"),
		@JoinColumn(name = "codeUniversiteEtape", referencedColumnName = "codeUniversite"),
		@JoinColumn(name = "codeVersionEtape", referencedColumnName = "codeVersionEtape")
	})
	// @formatter:on
	private Etape etape;

	@ManyToOne
	@JoinColumn(name = "idEtudiant", nullable = false)
	private Etudiant etudiant;

	@ManyToOne
	@JoinColumn(name = "idIndemnisation", nullable = false)
	private Indemnisation indemnisation;

	@ManyToOne
	@JoinColumn(name = "codeLangueConvention", nullable = false)
	private LangueConvention langueConvention;

	@ManyToOne
	@JoinColumn(name = "idModeValidationStage", nullable = false)
	private ModeValidationStage modeValidationStage;

	@ManyToOne
	@JoinColumn(name = "idNatureTravail", nullable = false)
	private NatureTravail natureTravail;

	@ManyToOne
	@JoinColumn(name = "idOffre")
	private Offre offre;

	@ManyToOne
	@JoinColumn(name = "idOrigineStage")
	private OrigineStage origineStage;

	@ManyToOne
	@JoinColumn(name = "idService")
	private Service service;

	@ManyToOne
	@JoinColumn(name = "idSignataire")
	private Contact contact2;

	@ManyToOne
	@JoinColumn(name = "idStructure")
	private Structure structure;

	@ManyToOne
	@JoinColumn(name = "idTempsTravail", nullable = false)
	private TempsTravail tempsTravail;

	@ManyToOne
	@JoinColumn(name = "idTheme", nullable = false)
	private Theme theme;

	@ManyToOne
	@JoinColumn(name = "idTypeConvention", nullable = false)
	private TypeConvention typeConvention;

	@ManyToOne
	// @formatter:off
	@JoinColumns({
		@JoinColumn(name = "codeUFR", referencedColumnName = "codeUFR"),
		@JoinColumn(name = "codeUniversiteUFR", referencedColumnName = "codeUniversite")
	})
	// @formatter:on
	private Ufr ufr;

	@ManyToOne
	@JoinColumn(name = "idUniteDureeExceptionnelle")
	private UniteDuree uniteDuree;

	@ManyToOne
	@JoinColumn(name = "idUniteGratification")
	private UniteGratification uniteGratification;

	@OneToMany(mappedBy = "convention")
	private List<ReponseEvaluation> reponseEvaluations;

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