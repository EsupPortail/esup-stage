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
 * The persistent class for the ContratOffre database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ContratOffre")
public class ContratOffre implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idContratOffre")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(nullable = false, length = 20)
	private String codeCtrl;

	@Column(name = "libelleContratOffre", nullable = false, length = 50)
	private String label;

	private boolean modifiable;

	@Column(name = "temEnServContratOffre", nullable = false, length = 1)
	private String temEnServ;

	@ManyToOne
	@JoinColumn(name = "idTypeOffre", nullable = false)
	private TypeOffre typeOffre;

	@OneToMany(mappedBy = "contratOffre")
	private List<Offre> offres;

	public Integer getIdContratOffre() {
		return this.id;
	}

	public Offre addOffre(Offre offre) {
		getOffres().add(offre);
		offre.setContratOffre(this);
		return offre;
	}

	public Offre removeOffre(Offre offre) {
		getOffres().remove(offre);
		offre.setContratOffre(null);
		return offre;
	}

}