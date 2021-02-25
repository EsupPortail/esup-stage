package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the OffreDiffusion database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "OffreDiffusion")
public class OffreDiffusion implements Serializable {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private OffreDiffusionPK id;

	@Column(nullable = false)
	private boolean estMiseEnAvant;

	@ManyToOne
	@JoinColumn(name = "idCentreGestion", nullable = false, insertable = false, updatable = false)
	private CentreGestion centreGestion;

	@ManyToOne
	@JoinColumn(name = "idOffre", nullable = false, insertable = false, updatable = false)
	private Offre offre;

}