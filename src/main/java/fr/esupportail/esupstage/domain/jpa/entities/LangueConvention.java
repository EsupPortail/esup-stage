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
 * The persistent class for the LangueConvention database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "LangueConvention")
public class LangueConvention implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "codeLangueConvention", unique = true, nullable = false, length = 2)
	private String code;

	@Column(name = "libelleLangueConvention", nullable = false, length = 100)
	private String label;

	@Column(name = "temEnServLangue", length = 1)
	private String temEnServ;

	@OneToMany(mappedBy = "langueConvention")
	private List<Convention> conventions;

	public Convention addConvention(Convention convention) {
		getConventions().add(convention);
		convention.setLangueConvention(this);
		return convention;
	}

	public Convention removeConvention(Convention convention) {
		getConventions().remove(convention);
		convention.setLangueConvention(null);
		return convention;
	}

}