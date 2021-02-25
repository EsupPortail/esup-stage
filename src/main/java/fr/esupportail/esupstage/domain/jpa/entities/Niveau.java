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
 * The persistent class for the Niveau database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Niveau")
public class Niveau implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idNiveau")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(name = "valeur", nullable = false)
	private Integer value;

	@OneToMany(mappedBy = "niveauBean")
	private List<Critere> criteres;

	public Critere addCritere(Critere critere) {
		getCriteres().add(critere);
		critere.setNiveauBean(this);
		return critere;
	}

	public Critere removeCritere(Critere critere) {
		getCriteres().remove(critere);
		critere.setNiveauBean(null);
		return critere;
	}

}