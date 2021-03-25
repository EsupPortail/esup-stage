package fr.esupportail.esupstage.services.beans;

import fr.esupportail.esupstage.domain.jpa.entities.Offre;
import fr.esupportail.esupstage.domain.jpa.entities.Service;
import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class PaysBean implements Serializable {

		private static final long serialVersionUID = 1L;

		private Integer id;
		private Integer actual;
		private Integer cog;
		private Integer crpay;
		private String iso2;
		private String lib;
		private boolean siretObligatoire;
		private String temEnServPays;
		private List<Offre> offres;
		private List<Service> services;
		private List<Structure> structures;

		public PaysBean() {
				super();
				this.offres = new LinkedList<>();
				this.services = new LinkedList<>();
				this.structures = new LinkedList<>();
		}

		public Offre addOffre(Offre offre) {
				getOffres().add(offre);
				//	offre.setPay(this);
				return offre;
		}

		public Offre removeOffre(Offre offre) {
				getOffres().remove(offre);
				offre.setPay(null);
				return offre;
		}

		public Service addService(Service service) {
				getServices().add(service);
				//	service.setPay(this);
				return service;
		}

		public Service removeService(Service service) {
				getServices().remove(service);
				service.setPay(null);
				return service;
		}

		public Structure addStructure(Structure structure) {
				getStructures().add(structure);
				//	structure.setPay(this);
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