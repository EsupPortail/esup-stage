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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the Structure database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Structure")
@NamedQuery(name = "Structure.findAll", query = "SELECT s FROM Structure s")
public class Structure extends Auditable<String> {

	private static final long serialVersionUID = 1L;

	@Id
    @GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer idStructure;

	@Lob
	private String activitePrincipale;

	@Column(length = 200)
	private String batimentResidence;

	@Column(length = 10)
	private String codeCommune;

	@Column(length = 20)
	private String codeEtab;

	@Column(length = 10)
	private String codePostal;

	@Column(length = 200)
	private String commune;

	private LocalDateTime dateStopValidation;

	private LocalDateTime dateValidation;

	@Column(nullable = false)
	private Integer estValidee;

	@Column(length = 20)
	private String fax;

	@Column(length = 50)
	private String groupe;

	private LocalDate infosAJour;

	@Column(length = 28)
	private String libCedex;

	@Column(length = 50)
	private String loginInfosAJour;

	@Column(length = 50)
	private String loginStopValidation;

	@Column(length = 50)
	private String loginValidation;

	@Column(length = 200)
	private String logo;

	@Column(length = 50)
	private String mail;

	@Column(length = 50)
	private String nomDirigeant;

	@Column(length = 14)
	private String numeroSiret;

	@Column(length = 50)
	private String prenomDirigeant;

	@Column(nullable = false, length = 150)
	private String raisonSociale;

	@Column(length = 200)
	private String siteWeb;

	@Column(length = 20)
	private String telephone;

	@Column(length = 1)
	private String temEnServStructure;

	@Column(nullable = false, length = 200)
	private String voie;

	@OneToMany(mappedBy = "structure")
	private List<AccordPartenariat> accordPartenariats;

	@OneToMany(mappedBy = "structure")
	private List<Convention> conventions;

	@OneToMany(mappedBy = "structure")
	private List<Offre> offres;

	@OneToMany(mappedBy = "structure")
	private List<Service> services;

	@ManyToOne
	@JoinColumn(name = "idEffectif", nullable = false)
	private Effectif effectif;

	@ManyToOne
	@JoinColumn(name = "codeNAF_N5")
	private NafN5 nafN5;

	@ManyToOne
	@JoinColumn(name = "idPays", nullable = false)
	private Pays pay;

	@ManyToOne
	@JoinColumn(name = "idStatutJuridique")
	private StatutJuridique statutJuridique;

	@ManyToOne
	@JoinColumn(name = "idTypeStructure", nullable = false)
	private TypeStructure typeStructure;

	public AccordPartenariat addAccordPartenariat(AccordPartenariat accordPartenariat) {
		getAccordPartenariats().add(accordPartenariat);
		accordPartenariat.setStructure(this);
		return accordPartenariat;
	}

	public AccordPartenariat removeAccordPartenariat(AccordPartenariat accordPartenariat) {
		getAccordPartenariats().remove(accordPartenariat);
		accordPartenariat.setStructure(null);
		return accordPartenariat;
	}

	public Convention addConvention(Convention convention) {
		getConventions().add(convention);
		convention.setStructure(this);
		return convention;
	}

	public Convention removeConvention(Convention convention) {
		getConventions().remove(convention);
		convention.setStructure(null);
		return convention;
	}

	public Offre addOffre(Offre offre) {
		getOffres().add(offre);
		offre.setStructure(this);
		return offre;
	}

	public Offre removeOffre(Offre offre) {
		getOffres().remove(offre);
		offre.setStructure(null);
		return offre;
	}

	public Service addService(Service service) {
		getServices().add(service);
		service.setStructure(this);
		return service;
	}

	public Service removeService(Service service) {
		getServices().remove(service);
		service.setStructure(null);
		return service;
	}

}