package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the FAP_N2 database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "FAP_N2")
public class FapN2 implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "codeFAP_N2", unique = true, nullable = false, length = 3)
	private String code;

	@Column(name = "libelle", nullable = false, length = 200)
	private String label;

	@ManyToOne
	@JoinColumn(name = "codeFAP_N1", nullable = false)
	private FapN1 fapN1;

	@OneToMany(mappedBy = "fapN2")
	private List<FapN3> fapN3s;

	public FapN3 addFapN3(FapN3 fapN3) {
		getFapN3s().add(fapN3);
		fapN3.setFapN2(this);
		return fapN3;
	}

	public FapN3 removeFapN3(FapN3 fapN3) {
		getFapN3s().remove(fapN3);
		fapN3.setFapN2(null);
		return fapN3;
	}

}