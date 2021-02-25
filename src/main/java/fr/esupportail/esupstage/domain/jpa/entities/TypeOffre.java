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
 * The persistent class for the TypeOffre database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "TypeOffre")
public class TypeOffre implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idTypeOffre")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(nullable = false, length = 20)
	private String codeCtrl;

	@Column(name = "libelleType", nullable = false, length = 50)
	private String label;

	private boolean modifiable;

	@Column(name = "temEnServTypeOffre", nullable = false, length = 1)
	private String temEnServ;

	@OneToMany(mappedBy = "typeOffre")
	private List<ContratOffre> contratOffres;

	@OneToMany(mappedBy = "typeOffre")
	private List<Offre> offres;

	public ContratOffre addContratOffre(ContratOffre contratOffre) {
		getContratOffres().add(contratOffre);
		contratOffre.setTypeOffre(this);
		return contratOffre;
	}

	public ContratOffre removeContratOffre(ContratOffre contratOffre) {
		getContratOffres().remove(contratOffre);
		contratOffre.setTypeOffre(null);
		return contratOffre;
	}

	public Offre addOffre(Offre offre) {
		getOffres().add(offre);
		offre.setTypeOffre(this);
		return offre;
	}

	public Offre removeOffre(Offre offre) {
		getOffres().remove(offre);
		offre.setTypeOffre(null);
		return offre;
	}

}