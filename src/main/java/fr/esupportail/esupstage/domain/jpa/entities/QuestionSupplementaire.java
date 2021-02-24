package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.Setter;

/**
 * The persistent class for the QuestionSupplementaire database table.
 *
 */
@Entity
@Getter
@Setter
@Table(name = "QuestionSupplementaire")
@NamedQuery(name = "QuestionSupplementaire.findAll", query = "SELECT q FROM QuestionSupplementaire q")
public class QuestionSupplementaire implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idQuestionSupplementaire")
    @GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(nullable = false)
	private Integer idPlacement;

	@Column(nullable = false, length = 200)
	private String question;

	@Column(nullable = false, length = 5)
	private String typeQuestion;

	// bi-directional many-to-one association to FicheEvaluation
	@ManyToOne
	@JoinColumn(name = "idFicheEvaluation", nullable = false)
	private FicheEvaluation ficheEvaluation;

	// bi-directional many-to-one association to ReponseSupplementaire
	@OneToMany(mappedBy = "questionSupplementaire")
	private List<ReponseSupplementaire> reponseSupplementaires;

	public QuestionSupplementaire() {
		super();
		this.reponseSupplementaires = new LinkedList<>();
	}

	public ReponseSupplementaire addReponseSupplementaire(ReponseSupplementaire reponseSupplementaire) {
		getReponseSupplementaires().add(reponseSupplementaire);
		reponseSupplementaire.setQuestionSupplementaire(this);
		return reponseSupplementaire;
	}

	public ReponseSupplementaire removeReponseSupplementaire(ReponseSupplementaire reponseSupplementaire) {
		getReponseSupplementaires().remove(reponseSupplementaire);
		reponseSupplementaire.setQuestionSupplementaire(null);
		return reponseSupplementaire;
	}

}