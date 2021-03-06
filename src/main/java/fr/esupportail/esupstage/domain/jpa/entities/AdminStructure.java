package fr.esupportail.esupstage.domain.jpa.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the AdminStructure database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "AdminStructure")
public class AdminStructure extends Auditable<String> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idAdminStructure")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	private LocalDateTime avantDerniereConnexion;

	private LocalDateTime derniereConnexion;

	@Column(length = 50)
	private String eppn;

	@Column(length = 50)
	private String login;

	@Column(length = 50)
	private String mail;

	@Column(length = 200)
	private String mdp;

	@Column(length = 50)
	private String nom;

	@Column(length = 50)
	private String prenom;

	@ManyToOne
	@JoinColumn(name = "idCivilite")
	private Civilite civilite;

}