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
 * The persistent class for the Ufr database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Ufr")
public class Ufr implements Serializable {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private UfrPK id;

	@Column(nullable = false, length = 100)
	private String libelleUFR;

	@OneToMany(mappedBy = "ufr")
	private List<Convention> conventions;

	public Convention addConvention(Convention convention) {
		getConventions().add(convention);
		convention.setUfr(this);
		return convention;
	}

	public Convention removeConvention(Convention convention) {
		getConventions().remove(convention);
		convention.setUfr(null);
		return convention;
	}

}