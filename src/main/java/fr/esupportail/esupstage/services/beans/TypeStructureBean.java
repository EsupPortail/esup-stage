package fr.esupportail.esupstage.services.beans;

import fr.esupportail.esupstage.domain.jpa.entities.StatutJuridique;
import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TypeStructureBean implements Serializable {

		private static final long serialVersionUID = 1L;
		private Integer idTypeStructure;
		private String libelleTypeStructure;
		private boolean modifiable;
		private boolean siretObligatoire;
		private String temEnServTypeStructure;
		private List<StatutJuridique> statutJuridiques;
		private List<Structure> structures;

		public StatutJuridique addStatutJuridique(StatutJuridique statutJuridique) {
				getStatutJuridiques().add(statutJuridique);
				//	statutJuridique.setTypeStructure(this);
				return statutJuridique;
		}

		public StatutJuridique removeStatutJuridique(StatutJuridique statutJuridique) {
				getStatutJuridiques().remove(statutJuridique);
				statutJuridique.setTypeStructure(null);
				return statutJuridique;
		}

		public Structure addStructure(Structure structure) {
				getStructures().add(structure);
				//	structure.setTypeStructure(this);
				return structure;
		}

		public Structure removeStructure(Structure structure) {
				getStructures().remove(structure);
				structure.setTypeStructure(null);
				return structure;
		}

		@Override public String toString() {
				return this.libelleTypeStructure;
		}
}