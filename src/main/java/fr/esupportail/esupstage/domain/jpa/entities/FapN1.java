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
 * The persistent class for the FAP_N1 database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "FAP_N1")
public class FapN1 implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "codeFAP_N1", unique = true, nullable = false, length = 1)
	private String code;

	@Column(name = "libelle", nullable = false, length = 200)
	private String label;

	@OneToMany(mappedBy = "fapN1")
	private List<FapN2> fapN2s;

	@OneToMany(mappedBy = "fapN1")
	private List<Offre> offres;

	public FapN2 addFapN2(FapN2 fapN2) {
		getFapN2s().add(fapN2);
		fapN2.setFapN1(this);
		return fapN2;
	}

	public FapN2 removeFapN2(FapN2 fapN2) {
		getFapN2s().remove(fapN2);
		fapN2.setFapN1(null);
		return fapN2;
	}

	public Offre addOffre(Offre offre) {
		getOffres().add(offre);
		offre.setFapN1(this);
		return offre;
	}

	public Offre removeOffre(Offre offre) {
		getOffres().remove(offre);
		offre.setFapN1(null);
		return offre;
	}

}