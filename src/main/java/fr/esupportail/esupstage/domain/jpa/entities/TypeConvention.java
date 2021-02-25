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
 * The persistent class for the TypeConvention database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "TypeConvention")
@NamedQuery(name = "TypeConvention.findAll", query = "SELECT t FROM TypeConvention t")
public class TypeConvention implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idTypeConvention")
    @GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(nullable = false, length = 20)
	private String codeCtrl;

	@Column(name = "libelleTypeConvention", nullable = false, length = 50)
	private String label;

	private boolean modifiable;

	@Column(name = "temEnServTypeConvention", nullable = false, length = 1)
	private String temEnServ;

	@OneToMany(mappedBy = "typeConvention")
	private List<Convention> conventions;

	public Convention addConvention(Convention convention) {
		getConventions().add(convention);
		convention.setTypeConvention(this);
		return convention;
	}

	public Convention removeConvention(Convention convention) {
		getConventions().remove(convention);
		convention.setTypeConvention(null);
		return convention;
	}

}