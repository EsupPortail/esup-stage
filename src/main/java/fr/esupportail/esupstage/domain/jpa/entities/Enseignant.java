package fr.esupportail.esupstage.domain.jpa.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the Enseignant database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Enseignant")
public class Enseignant extends Auditable<String> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idEnseignant")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(length = 45)
	private String batiment;

	@Column(length = 20)
	private String bureau;

	@Column(length = 250)
	private String campus;

	@Column(nullable = false, length = 10)
	private String codeUniversite;

	@Column(length = 50)
	private String fax;

	@Column(length = 50)
	private String mail;

	@Column(nullable = false, length = 50)
	private String nom;

	@Column(nullable = false, length = 50)
	private String prenom;

	@Column(length = 30)
	private String telephone;

	@Column(length = 50)
	private String typePersonne;

	@Column(nullable = false, length = 50)
	private String uidEnseignant;

	@OneToMany(mappedBy = "enseignant")
	private List<Convention> conventions;

	@ManyToOne
	// @formatter:off
	@JoinColumns({
		@JoinColumn(name = "codeAffectation", referencedColumnName = "codeAffectation"),
		@JoinColumn(name = "codeUniversiteAffectation", referencedColumnName = "codeUniversite")
	})
	// @formatter:on
	private Affectation affectation;

	public Convention addConvention(Convention convention) {
		getConventions().add(convention);
		convention.setEnseignant(this);
		return convention;
	}

	public Convention removeConvention(Convention convention) {
		getConventions().remove(convention);
		convention.setEnseignant(null);
		return convention;
	}

}