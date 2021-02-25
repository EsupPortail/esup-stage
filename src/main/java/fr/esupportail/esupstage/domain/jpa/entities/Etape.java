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
 * The persistent class for the Etape database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Etape")
public class Etape implements Serializable {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private EtapePK id;

	@Column(nullable = false, length = 200)
	private String libelleEtape;

	@OneToMany(mappedBy = "etape")
	private List<Convention> conventions;

	public Convention addConvention(Convention convention) {
		getConventions().add(convention);
		convention.setEtape(this);
		return convention;
	}

	public Convention removeConvention(Convention convention) {
		getConventions().remove(convention);
		convention.setEtape(null);
		return convention;
	}

}