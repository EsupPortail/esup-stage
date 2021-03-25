package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.Setter;

/**
 * The persistent class for the Pays database table.
 *
 */
@Entity
@Getter
@Setter
@Table(name = "Pays")
public class Pays implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idPays")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(nullable = false)
	private Integer actual;

	@Column(name = "COG", nullable = false)
	private Integer cog;

	@Column(name = "CRPAY")
	private Integer crpay;

	@Column(name = "ISO2", length = 2)
	private String iso2;

	@Column(nullable = false, length = 70)
	private String lib;

	@Column(nullable = false)
	private boolean siretObligatoire;

	@Column(name = "temEnServPays", nullable = false, length = 1)
	private String temEnServ;

	@OneToMany(mappedBy = "pay")
	private List<Offre> offres;

	@OneToMany(mappedBy = "pay")
	private List<Service> services;

	@OneToMany(mappedBy = "pay")
	private List<Structure> structures;

	public Pays() {
		super();
		this.offres = new LinkedList<>();
		this.services = new LinkedList<>();
		this.structures = new LinkedList<>();
	}

	public Offre addOffre(Offre offre) {
		getOffres().add(offre);
		offre.setPay(this);
		return offre;
	}

	public Offre removeOffre(Offre offre) {
		getOffres().remove(offre);
		offre.setPay(null);
		return offre;
	}

	public Service addService(Service service) {
		getServices().add(service);
		service.setPay(this);
		return service;
	}

	public Service removeService(Service service) {
		getServices().remove(service);
		service.setPay(null);
		return service;
	}

	public Structure addStructure(Structure structure) {
		getStructures().add(structure);
		structure.setPay(this);
		return structure;
	}

	public Structure removeStructure(Structure structure) {
		getStructures().remove(structure);
		structure.setPay(null);
		return structure;
	}

		@Override public String toString() {
				return lib;
		}

}