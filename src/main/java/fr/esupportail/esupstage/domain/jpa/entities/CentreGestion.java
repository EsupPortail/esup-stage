package fr.esupportail.esupstage.domain.jpa.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the CentreGestion database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "CentreGestion")
public class CentreGestion extends Auditable<String> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idCentreGestion")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(length = 200)
	private String adresse;

	@Column(nullable = false)
	private boolean autorisationEtudiantCreationConvention;

	@Column(nullable = false)
	private boolean autoriserImpressionConvention;

	@Transient
	private boolean choixAnneeApresDebutAnnee;

	@Transient
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
	private boolean depotAnonyme;

	@Column(length = 20)
	private String fax;

	@Column(nullable = false)
	private Integer idModeValidationStage;

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

	@ManyToOne
	@JoinColumn(name = "idCentreGestionSuperViseur")
	private CentreGestionSuperViseur centreGestionSuperViseur;

	@ManyToOne
	@JoinColumn(name = "codeConfidentialite", nullable = false)
	private Confidentialite confidentialite;

	@ManyToOne
	@JoinColumn(name = "idFichier")
	private Fichier fichier;

	@ManyToOne
	@JoinColumn(name = "idNiveauCentre", nullable = false)
	private NiveauCentre niveauCentre;

	@OneToMany(mappedBy = "centreGestion")
	private List<Contact> contacts;

	@OneToMany(mappedBy = "centreGestion")
	private List<Convention> conventions;

	@OneToMany(mappedBy = "centreGestion")
	private List<CritereGestion> critereGestions;

	@OneToMany(mappedBy = "centreGestion")
	private List<FicheEvaluation> ficheEvaluations;

	@OneToMany(mappedBy = "centreGestion")
	private List<Offre> offres;

	@OneToMany(mappedBy = "centreGestion")
	private List<OffreDiffusion> offreDiffusions;

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