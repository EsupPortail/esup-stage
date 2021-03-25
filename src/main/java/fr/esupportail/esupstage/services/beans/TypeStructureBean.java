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

		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		@Column(unique = true, nullable = false)
		private Integer idTypeStructure;

		@Column(nullable = false, length = 100)
		private String libelleTypeStructure;

		private boolean modifiable;

		@Column(nullable = false)
		private boolean siretObligatoire;

		@Column(nullable = false, length = 1)
		private String temEnServTypeStructure;

		@OneToMany(mappedBy = "typeStructure")
		private List<StatutJuridique> statutJuridiques;

		@OneToMany(mappedBy = "typeStructure")
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