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
 * The persistent class for the NiveauFormation database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "NiveauFormation")
public class NiveauFormation implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idNiveauFormation")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(name = "libelleNiveauFormation", nullable = false, length = 45)
	private String label;

	private boolean modifiable;

	@Column(name = "temEnServNiveauForm", nullable = false, length = 1)
	private String temEnServ;

	@OneToMany(mappedBy = "niveauFormation")
	private List<Offre> offres;

	public Offre addOffre(Offre offre) {
		getOffres().add(offre);
		offre.setNiveauFormation(this);
		return offre;
	}

	public Offre removeOffre(Offre offre) {
		getOffres().remove(offre);
		offre.setNiveauFormation(null);
		return offre;
	}

}