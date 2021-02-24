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
 * The persistent class for the UniteGratification database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "UniteGratification")
@NamedQuery(name = "UniteGratification.findAll", query = "SELECT u FROM UniteGratification u")
public class UniteGratification implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
    @GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer idUniteGratification;

	@Column(nullable = false, length = 50)
	private String libelleUniteGratification;

	@Column(nullable = false, length = 1)
	private String temEnServGrat;

	@OneToMany(mappedBy = "uniteGratification")
	private List<Convention> conventions;

	public Convention addConvention(Convention convention) {
		getConventions().add(convention);
		convention.setUniteGratification(this);
		return convention;
	}

	public Convention removeConvention(Convention convention) {
		getConventions().remove(convention);
		convention.setUniteGratification(null);
		return convention;
	}

}