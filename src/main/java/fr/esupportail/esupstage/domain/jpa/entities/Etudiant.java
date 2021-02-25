package fr.esupportail.esupstage.domain.jpa.entities;

import java.time.LocalDate;
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
 * The persistent class for the Etudiant database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Etudiant")
public class Etudiant extends Auditable<String> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idEtudiant")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(length = 1)
	private String codeSexe;

	@Column(nullable = false, length = 50)
	private String codeUniversite;

	private LocalDate dateNais;

	@Column(nullable = false, length = 50)
	private String identEtudiant;

	@Column(length = 100)
	private String mail;

	@Column(nullable = false, length = 50)
	private String nom;

	@Column(length = 50)
	private String nomMarital;

	@Column(nullable = false, length = 20)
	private String numEtudiant;

	@Column(length = 15)
	private String numSS;

	@Column(nullable = false, length = 50)
	private String prenom;

	@OneToMany(mappedBy = "etudiant")
	private List<Convention> conventions;

	public Convention addConvention(Convention convention) {
		getConventions().add(convention);
		convention.setEtudiant(this);
		return convention;
	}

	public Convention removeConvention(Convention convention) {
		getConventions().remove(convention);
		convention.setEtudiant(null);
		return convention;
	}

}