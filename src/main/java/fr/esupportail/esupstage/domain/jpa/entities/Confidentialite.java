package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the Confidentialite database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Confidentialite")
public class Confidentialite implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "codeConfidentialite", unique = true, nullable = false, length = 1)
	private String code;

	@Column(name = "libelleConfidentialite", nullable = false, length = 50)
	private String label;

	@Column(name = "temEnServConfid", nullable = false, length = 1)
	private String temEnServ;

	@OneToMany(mappedBy = "confidentialite")
	private List<CentreGestion> centreGestions;

	public CentreGestion addCentreGestion(CentreGestion centreGestion) {
		getCentreGestions().add(centreGestion);
		centreGestion.setConfidentialite(this);
		return centreGestion;
	}

	public CentreGestion removeCentreGestion(CentreGestion centreGestion) {
		getCentreGestions().remove(centreGestion);
		centreGestion.setConfidentialite(null);
		return centreGestion;
	}

}