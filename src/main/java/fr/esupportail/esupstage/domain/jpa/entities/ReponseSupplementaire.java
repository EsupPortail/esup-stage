package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the ReponseSupplementaire database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ReponseSupplementaire")
public class ReponseSupplementaire implements Serializable {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private ReponseSupplementairePK id;

	private boolean reponseBool;

	private Integer reponseInt;

	@Lob
	private String reponseTxt;

	@ManyToOne
	@JoinColumn(name = "idConvention", nullable = false, insertable = false, updatable = false)
	private Convention convention;

	@ManyToOne
	@JoinColumn(name = "idQuestionSupplementaire", nullable = false, insertable = false, updatable = false)
	private QuestionSupplementaire questionSupplementaire;

}