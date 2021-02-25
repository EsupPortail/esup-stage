package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;
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
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the DroitAdministration database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "DroitAdministration")
public class DroitAdministration implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idDroitAdministration")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(name = "libelleDroitAdministration", nullable = false, length = 50)
	private String label;

	@Column(name = "temEnServDroitAdmin", nullable = false, length = 1)
	private String temEnServ;

	@OneToMany(mappedBy = "droitAdministration")
	private List<PersonnelCentreGestion> personnelCentreGestions;

	public PersonnelCentreGestion addPersonnelCentreGestion(PersonnelCentreGestion personnelCentreGestion) {
		getPersonnelCentreGestions().add(personnelCentreGestion);
		personnelCentreGestion.setDroitAdministration(this);
		return personnelCentreGestion;
	}

	public PersonnelCentreGestion removePersonnelCentreGestion(PersonnelCentreGestion personnelCentreGestion) {
		getPersonnelCentreGestions().remove(personnelCentreGestion);
		personnelCentreGestion.setDroitAdministration(null);
		return personnelCentreGestion;
	}

}
