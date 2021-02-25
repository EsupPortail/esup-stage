package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the TempsTravail database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "TempsTravail")
@NamedQuery(name = "TempsTravail.findAll", query = "SELECT t FROM TempsTravail t")
public class TempsTravail implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idTempsTravail")
    @GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(nullable = false, length = 20)
	private String codeCtrl;

	@Column(name = "libelleTempsTravail", nullable = false, length = 200)
	private String label;

	private boolean modifiable;

	@Column(name = "temEnServTempsTravail", nullable = false, length = 1)
	private String temEnServ;

	@OneToMany(mappedBy = "tempsTravail")
	private List<Convention> conventions;

	@OneToMany(mappedBy = "tempsTravail")
	private List<Offre> offres;

	public Convention addConvention(Convention convention) {
		getConventions().add(convention);
		convention.setTempsTravail(this);
		return convention;
	}

	public Convention removeConvention(Convention convention) {
		getConventions().remove(convention);
		convention.setTempsTravail(null);
		return convention;
	}

	public Offre addOffre(Offre offre) {
		getOffres().add(offre);
		offre.setTempsTravail(this);
		return offre;
	}

	public Offre removeOffre(Offre offre) {
		getOffres().remove(offre);
		offre.setTempsTravail(null);
		return offre;
	}

}