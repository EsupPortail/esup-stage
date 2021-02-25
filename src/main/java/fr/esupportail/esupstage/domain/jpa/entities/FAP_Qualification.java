package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the FAP_Qualification database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "FAP_Qualification")
public class FAP_Qualification implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "numFAP_Qualification")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(name = "libelleQualification", nullable = false, length = 100)
	private String label;

	@OneToMany(mappedBy = "fapQualification")
	private List<FapN3> fapN3s;

	@ManyToOne
	@JoinColumn(name = "idQualificationSimplifiee", nullable = false)
	private FAP_QualificationSimplifiee fapQualificationSimplifiee;

}