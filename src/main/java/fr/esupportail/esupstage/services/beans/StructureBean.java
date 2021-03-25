package fr.esupportail.esupstage.services.beans;

import fr.esupportail.esupstage.domain.jpa.entities.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StructureBean implements Serializable {

		private static final long serialVersionUID = 1L;

		private Integer idStructure;
		private String activitePrincipale;
		private String batimentResidence;
		private String codeCommune;
		private String codeEtab;
		private String codePostal;
		private String commune;
		private Date dateStopValidation;
		private Date dateValidation;
		private Integer estValidee;
		private String fax;
		private String groupe;
		private Date infosAJour;
		private String libCedex;
		private String loginInfosAJour;
		private String loginStopValidation;
		private String loginValidation;
		private String logo;
		private String mail;
		private String nomDirigeant;
		private String numeroSiret;
		private String prenomDirigeant;
		private String raisonSociale;
		private String siteWeb;
		private String telephone;
		private String temEnServStructure;
		private String voie;
		private List<AccordPartenariat> accordPartenariats;
		private List<Convention> conventions;
		private List<Offre> offres;
		private List<Service> services;
		private Effectif effectif;
		private NafN5 nafN5;
		private Pays pay;
		private StatutJuridique statutJuridique;
		private TypeStructure typeStructure;

		public AccordPartenariat addAccordPartenariat(AccordPartenariat accordPartenariat) {
				getAccordPartenariats().add(accordPartenariat);
				//		accordPartenariat.setStructure(this);
				return accordPartenariat;
		}

		public AccordPartenariat removeAccordPartenariat(AccordPartenariat accordPartenariat) {
				getAccordPartenariats().remove(accordPartenariat);
				accordPartenariat.setStructure(null);
				return accordPartenariat;
		}

		public Convention addConvention(Convention convention) {
				getConventions().add(convention);
				//			convention.setStructure(this);
				return convention;
		}

		public Convention removeConvention(Convention convention) {
				getConventions().remove(convention);
				convention.setStructure(null);
				return convention;
		}

		public Offre addOffre(Offre offre) {
				getOffres().add(offre);
				//	offre.setStructure(this);
				return offre;
		}

		public Offre removeOffre(Offre offre) {
				getOffres().remove(offre);
				offre.setStructure(null);
				return offre;
		}

		public Service addService(Service service) {
				getServices().add(service);
				//		service.setStructure(this);
				return service;
		}

		public Service removeService(Service service) {
				getServices().remove(service);
				service.setStructure(null);
				return service;
		}
}
