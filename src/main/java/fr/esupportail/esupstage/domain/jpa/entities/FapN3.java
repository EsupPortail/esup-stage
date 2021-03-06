package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the FAP_N3 database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "FAP_N3")
public class FapN3 implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "codeFAP_N3", unique = true, nullable = false, length = 5)
	private String code;

	@Column(name = "libelle", nullable = false, length = 200)
	private String label;

	@ManyToOne
	@JoinColumn(name = "codeFAP_N2", nullable = false)
	private FapN2 fapN2;

	@ManyToOne
	@JoinColumn(name = "numFAP_Qualification", nullable = false)
	private FAP_Qualification fapQualification;

}