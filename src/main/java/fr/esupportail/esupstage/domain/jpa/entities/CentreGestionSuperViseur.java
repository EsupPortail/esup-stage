package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;
import java.util.ArrayList;
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
import lombok.Setter;

/**
 * The persistent class for the CentreGestionSuperViseur database table.
 *
 */
@Entity
@Getter
@Setter
@Table(name = "CentreGestionSuperViseur")
public class CentreGestionSuperViseur implements Serializable {

	public CentreGestionSuperViseur() {
		this.centreGestions = new ArrayList<>();
	}

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idCentreGestionSuperViseur")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(name = "nomCentreSuperViseur", nullable = false, length = 100)
	private String name;

	@OneToMany(mappedBy = "centreGestionSuperViseur")
	private List<CentreGestion> centreGestions;

	public CentreGestion addCentreGestion(CentreGestion centreGestion) {
		getCentreGestions().add(centreGestion);
		centreGestion.setCentreGestionSuperViseur(this);
		return centreGestion;
	}

	public CentreGestion removeCentreGestion(CentreGestion centreGestion) {
		getCentreGestions().remove(centreGestion);
		centreGestion.setCentreGestionSuperViseur(null);
		return centreGestion;
	}

}