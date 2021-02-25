package fr.esupportail.esupstage.domain.jpa.entities;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.Setter;

/**
 * The persistent class for the Service database table.
 *
 */
@Entity
@Getter
@Setter
@Table(name = "Service")
@NamedQuery(name = "Service.findAll", query = "SELECT s FROM Service s")
public class Service extends Auditable<String> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idService")
    @GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(length = 200)
	private String batimentResidence;

	@Column(length = 10)
	private String codeCommune;

	@Column(nullable = false, length = 10)
	private String codePostal;

	@Column(length = 200)
	private String commune;

	private LocalDate infosAJour;

	@Column(length = 50)
	private String loginInfosAJour;

	@Column(nullable = false, length = 70)
	private String nom;

	@Column(length = 20)
	private String telephone;

	@Column(nullable = false, length = 200)
	private String voie;

	// bi-directional many-to-one association to Avenant
	@OneToMany(mappedBy = "service")
	private List<Avenant> avenants;

	// bi-directional many-to-one association to Contact
	@OneToMany(mappedBy = "service")
	private List<Contact> contacts;

	// bi-directional many-to-one association to Convention
	@OneToMany(mappedBy = "service")
	private List<Convention> conventions;

	// bi-directional many-to-one association to Pay
	@ManyToOne
	@JoinColumn(name = "idPays", nullable = false)
	private Pays pay;

	// bi-directional many-to-one association to Structure
	@ManyToOne
	@JoinColumn(name = "idStructure", nullable = false)
	private Structure structure;

	public Service() {
		super();
		this.avenants = new LinkedList<>();
		this.contacts = new LinkedList<>();
		this.conventions = new LinkedList<>();
	}

	public Avenant addAvenant(Avenant avenant) {
		getAvenants().add(avenant);
		avenant.setService(this);
		return avenant;
	}

	public Avenant removeAvenant(Avenant avenant) {
		getAvenants().remove(avenant);
		avenant.setService(null);
		return avenant;
	}

	public Contact addContact(Contact contact) {
		getContacts().add(contact);
		contact.setService(this);
		return contact;
	}

	public Contact removeContact(Contact contact) {
		getContacts().remove(contact);
		contact.setService(null);
		return contact;
	}

	public Convention addConvention(Convention convention) {
		getConventions().add(convention);
		convention.setService(this);
		return convention;
	}

	public Convention removeConvention(Convention convention) {
		getConventions().remove(convention);
		convention.setService(null);
		return convention;
	}

}