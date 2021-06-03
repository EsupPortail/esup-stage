package fr.esupportail.esupstage.services.beans;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


import org.hibernate.annotations.GenericGenerator;

import fr.esupportail.esupstage.domain.jpa.entities.AccordPartenariat;
import fr.esupportail.esupstage.domain.jpa.entities.Avenant;
import fr.esupportail.esupstage.domain.jpa.entities.CentreGestion;
import fr.esupportail.esupstage.domain.jpa.entities.Civilite;
import fr.esupportail.esupstage.domain.jpa.entities.Convention;
import fr.esupportail.esupstage.domain.jpa.entities.Offre;
import fr.esupportail.esupstage.domain.jpa.entities.Service;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactBean {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private LocalDateTime avantDerniereConnexion;
	private String commentaire;
	private LocalDateTime derniereConnexion;
	private String fax;
	private String fonction;
	private LocalDate infosAJour;
	private String login;
	private String loginInfosAJour;
	private String mail;
	private String mdp;
	private String nom;
	private String prenom;
	private String tel;
	private List<AccordPartenariat> accordPartenariats;
	private List<Avenant> avenants;
	private CentreGestion centreGestion;
	private Civilite civilite;
	private Service service;
	private List<Convention> conventions1;
	private List<Convention> conventions2;
	private List<Offre> offres1;
	private List<Offre> offres2;
	private List<Offre> offres3;

	public AccordPartenariat addAccordPartenariat(AccordPartenariat accordPartenariat) {
		accordPartenariats.add(accordPartenariat);
		//accordPartenariat.setContact(this);
		return accordPartenariat;
	}

	public AccordPartenariat removeAccordPartenariat(AccordPartenariat accordPartenariat) {
		accordPartenariats.remove(accordPartenariat);
		accordPartenariat.setContact(null);
		return accordPartenariat;
	}

	public Avenant addAvenant(Avenant avenant) {
		avenants.add(avenant);
		//avenant.setContact(this);
		return avenant;
	}

	public Avenant removeAvenant(Avenant avenant) {
		avenants.remove(avenant);
		avenant.setContact(null);
		return avenant;
	}

	public Convention addConventions1(Convention conventions1) {
		this.conventions1.add(conventions1);
		//conventions1.setContact1(this);
		return conventions1;
	}

	public Convention removeConventions1(Convention conventions1) {
		this.conventions1.remove(conventions1);
		//conventions1.setContact1(null);
		return conventions1;
	}

	public Convention addConventions(Convention conventions2) {
		this.conventions2.add(conventions2);
		//conventions2.setContact2(this);
		return conventions2;
	}

	public Convention removeConventions2(Convention conventions2) {
		this.conventions2.remove(conventions2);
		//conventions2.setContact2(null);
		return conventions2;
	}

	@Override
	public String toString() {
		return this.prenom + " " + this.nom;
	}

}
