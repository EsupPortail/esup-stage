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
 * The persistent class for the ModeValidationStage database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ModeValidationStage")
public class ModeValidationStage implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idModeValidationStage")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(name = "libelleModeValidationStage", nullable = false, length = 150)
	private String label;

	private boolean modifiable;

	@Column(name = "temEnServModeValid", nullable = false, length = 1)
	private String temEnServ;

	@OneToMany(mappedBy = "modeValidationStage")
	private List<Convention> conventions;

	public Convention addConvention(Convention convention) {
		getConventions().add(convention);
		convention.setModeValidationStage(this);
		return convention;
	}

	public Convention removeConvention(Convention convention) {
		getConventions().remove(convention);
		convention.setModeValidationStage(null);
		return convention;
	}

}