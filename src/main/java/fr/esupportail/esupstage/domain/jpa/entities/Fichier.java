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
 * The persistent class for the Fichiers database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Fichiers")
public class Fichier implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idFichier")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(name = "nomFichier", nullable = false, length = 255)
	private String localFileName;

	@Column(name = "nomReel", length = 255)
	private String originalFileName;

	@OneToMany(mappedBy = "fichier")
	private List<CentreGestion> centreGestions;

	@OneToMany(mappedBy = "fichier")
	private List<Offre> offres;

	public void setCentreGestions(List<CentreGestion> centreGestions) {
		this.centreGestions = centreGestions;
	}

	public CentreGestion addCentreGestion(CentreGestion centreGestion) {
		getCentreGestions().add(centreGestion);
		centreGestion.setFichier(this);
		return centreGestion;
	}

	public CentreGestion removeCentreGestion(CentreGestion centreGestion) {
		getCentreGestions().remove(centreGestion);
		centreGestion.setFichier(null);
		return centreGestion;
	}

}