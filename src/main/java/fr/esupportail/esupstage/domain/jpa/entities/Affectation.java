package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the Affectation database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Affectation")
public class Affectation implements Serializable {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private AffectationPK id;

	@Column(length = 150)
	private String libelleAffectation;

	@OneToMany(mappedBy = "affectation")
	private List<Enseignant> enseignants;

	@OneToMany(mappedBy = "affectation")
	private List<PersonnelCentreGestion> personnelCentreGestions;

	public Enseignant addEnseignant(Enseignant enseignant) {
		getEnseignants().add(enseignant);
		enseignant.setAffectation(this);
		return enseignant;
	}

	public Enseignant removeEnseignant(Enseignant enseignant) {
		getEnseignants().remove(enseignant);
		enseignant.setAffectation(null);
		return enseignant;
	}

	public List<PersonnelCentreGestion> getPersonnelCentreGestions() {
		return this.personnelCentreGestions;
	}

	public void setPersonnelCentreGestions(List<PersonnelCentreGestion> personnelCentreGestions) {
		this.personnelCentreGestions = personnelCentreGestions;
	}

	public PersonnelCentreGestion addPersonnelCentreGestion(PersonnelCentreGestion personnelCentreGestion) {
		getPersonnelCentreGestions().add(personnelCentreGestion);
		personnelCentreGestion.setAffectation(this);
		return personnelCentreGestion;
	}

	public PersonnelCentreGestion removePersonnelCentreGestion(PersonnelCentreGestion personnelCentreGestion) {
		getPersonnelCentreGestions().remove(personnelCentreGestion);
		personnelCentreGestion.setAffectation(null);
		return personnelCentreGestion;
	}

}