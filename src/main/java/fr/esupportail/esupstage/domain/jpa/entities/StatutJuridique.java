package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the StatutJuridique database table.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "StatutJuridique")
public class StatutJuridique implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idStatutJuridique")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(name = "libelleStatutJuridique", nullable = false, length = 100)
	private String label;

	private boolean modifiable;

	@Column(name = "temEnServStatut", nullable = false, length = 1)
	private String temEnServ;

	@ManyToOne
	@JoinColumn(name = "idTypeStructure", nullable = false)
	private TypeStructure typeStructure;

	@OneToMany(mappedBy = "statutJuridique")
	private List<Structure> structures;

	public Structure addStructure(Structure structure) {
		getStructures().add(structure);
		structure.setStatutJuridique(this);
		return structure;
	}

	public Structure removeStructure(Structure structure) {
		getStructures().remove(structure);
		structure.setStatutJuridique(null);
		return structure;
	}

	@Override
	public String toString() {
		return this.label;
	}

}