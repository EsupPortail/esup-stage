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
 * The persistent class for the NiveauCentre database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "NiveauCentre")
public class NiveauCentre implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idNiveauCentre")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(name = "libelleNiveauCentre", nullable = false, length = 50)
	private String label;

	@Column(name = "temEnServNiveauCentre", nullable = false, length = 1)
	private String temEnServ;

	@OneToMany(mappedBy = "niveauCentre")
	private List<CentreGestion> centreGestions;

	public CentreGestion addCentreGestion(CentreGestion centreGestion) {
		getCentreGestions().add(centreGestion);
		centreGestion.setNiveauCentre(this);
		return centreGestion;
	}

	public CentreGestion removeCentreGestion(CentreGestion centreGestion) {
		getCentreGestions().remove(centreGestion);
		centreGestion.setNiveauCentre(null);
		return centreGestion;
	}

}