package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the ModeCandidature database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ModeCandidature")
public class ModeCandidature implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idModeCandidature")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(nullable = false, length = 20)
	private String codeCtrl;

	@Column(name = "libelleModeCandidature", nullable = false, length = 50)
	private String label;

	@Column(name = "temEnServModeCandidature", nullable = false, length = 1)
	private String temEnServ;

	@ManyToMany(mappedBy = "modeCandidatures")
	private List<Offre> offres;

}
