package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;
import java.util.LinkedList;
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
 * The persistent class for the OrigineStage database table.
 *
 */
@Entity
@Getter
@Setter
@Table(name = "OrigineStage")
public class OrigineStage implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idOrigineStage")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(name = "libelleOrigineStage", nullable = false, length = 45)
	private String label;

	private boolean modifiable;

	@Column(name = "temEnServOrigineStage", nullable = false, length = 1)
	private String temEnServ;

	@OneToMany(mappedBy = "origineStage")
	private List<Convention> conventions;

	public OrigineStage() {
		super();
		this.conventions = new LinkedList<>();
	}

	public Convention addConvention(Convention convention) {
		getConventions().add(convention);
		convention.setOrigineStage(this);
		return convention;
	}

	public Convention removeConvention(Convention convention) {
		getConventions().remove(convention);
		convention.setOrigineStage(null);
		return convention;
	}

}