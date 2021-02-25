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
 * The persistent class for the UniteDuree database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "UniteDuree")
public class UniteDuree implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idUniteDuree")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(name = "libelleUniteDuree", nullable = false, length = 100)
	private String label;

	@Column(name = "temEnServUniteDuree", nullable = false, length = 1)
	private String temEnServ;

	@OneToMany(mappedBy = "uniteDuree")
	private List<Convention> conventions;

	@OneToMany(mappedBy = "uniteDuree")
	private List<Offre> offres;

	public Convention addConvention(Convention convention) {
		getConventions().add(convention);
		convention.setUniteDuree(this);
		return convention;
	}

	public Convention removeConvention(Convention convention) {
		getConventions().remove(convention);
		convention.setUniteDuree(null);
		return convention;
	}

	public Offre addOffre(Offre offre) {
		getOffres().add(offre);
		offre.setUniteDuree(this);
		return offre;
	}

	public Offre removeOffre(Offre offre) {
		getOffres().remove(offre);
		offre.setUniteDuree(null);
		return offre;
	}

}