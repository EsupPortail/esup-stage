package fr.esupportail.esupstage.domain.jpa.entities;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.Setter;

/**
 * The persistent class for the PersonnelCentreGestion database table.
 *
 */
@Entity
@Getter
@Setter
@Table(name = "PersonnelCentreGestion")
public class PersonnelCentreGestion extends Auditable<String> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idPersonnelCentreGestion")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	private boolean alertesMail;

	@Column(length = 50)
	private String batiment;

	@Column(length = 50)
	private String bureau;

	@Column(length = 250)
	private String campus;

	@Column(length = 50)
	private String codeUniversite;

	private boolean droitEvaluationEnseignant;

	private boolean droitEvaluationEntreprise;

	private boolean droitEvaluationEtudiant;

	@Column(length = 50)
	private String fax;

	@Column(length = 50)
	private String fonction;

	@Column(nullable = false)
	private boolean impressionConvention;

	@Column(length = 50)
	private String mail;

	@Column(nullable = false, length = 50)
	private String nom;

	@Column(nullable = false, length = 50)
	private String prenom;

	@Column(length = 50)
	private String telephone;

	@Column(length = 50)
	private String typePersonne;

	@Column(nullable = false, length = 50)
	private String uidPersonnel;

	@OneToMany(mappedBy = "personnelCentreGestion")
	private List<Offre> offres;

	@ManyToOne
	// @formatter:off
	@JoinColumns({
		@JoinColumn(name = "codeAffectation", referencedColumnName = "codeAffectation", nullable = false),
		@JoinColumn(name = "codeUniversiteAffectation", referencedColumnName = "codeUniversite", nullable = false)
	})
	// @formatter:on
	private Affectation affectation;

	@ManyToOne
	@JoinColumn(name = "idCentreGestion", nullable = false)
	private CentreGestion centreGestion;

	@ManyToOne
	@JoinColumn(name = "idCivilite")
	private Civilite civilite;

	@ManyToOne
	@JoinColumn(name = "idDroitAdministration", nullable = false)
	private DroitAdministration droitAdministration;

	public PersonnelCentreGestion() {
		super();
		this.offres = new LinkedList<>();
	}

	public Offre addOffre(Offre offre) {
		getOffres().add(offre);
		offre.setPersonnelCentreGestion(this);
		return offre;
	}

	public Offre removeOffre(Offre offre) {
		getOffres().remove(offre);
		offre.setPersonnelCentreGestion(null);
		return offre;
	}

}