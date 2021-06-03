package fr.esupportail.esupstage.services.beans;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;

import fr.esupportail.esupstage.domain.jpa.entities.Avenant;
import fr.esupportail.esupstage.domain.jpa.entities.Contact;
import fr.esupportail.esupstage.domain.jpa.entities.Convention;
import fr.esupportail.esupstage.domain.jpa.entities.Pays;
import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceBean {

	private Integer id;
	private String batimentResidence;
	private String codeCommune;
	private String codePostal;
	private String commune;
	private LocalDate infosAJour;
	private String loginInfosAJour;
	private String nom;
	private String telephone;
	private String voie;
	private List<Avenant> avenants;
	private List<Contact> contacts;
	private List<Convention> conventions;
	private Pays pay;
	private Structure structure;

	@Override
	public String toString() {
		return this.nom;
	}

	public ServiceBean() {
		super();
		this.avenants = new LinkedList<>();
		this.contacts = new LinkedList<>();
		this.conventions = new LinkedList<>();
	}
}
