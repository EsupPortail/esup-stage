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
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the FicheEvaluation database table.
 *
 */
@Entity
@Table(name = "FicheEvaluation")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "FicheEvaluation.findAll", query = "SELECT f FROM FicheEvaluation f")
public class FicheEvaluation implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idFicheEvaluation;
    private boolean questionEnsI1;
    private boolean questionEnsI2;
    private boolean questionEnsI3;
    private boolean questionEnsII1;
    private boolean questionEnsII10;
    private boolean questionEnsII11;
    private boolean questionEnsII2;
    private boolean questionEnsII3;
    private boolean questionEnsII4;
    private boolean questionEnsII5;
    private boolean questionEnsII6;
    private boolean questionEnsII7;
    private boolean questionEnsII8;
    private boolean questionEnsII9;
    private boolean questionEnt1;
    private boolean questionEnt10;
    private boolean questionEnt11;
    private boolean questionEnt12;
    private boolean questionEnt13;
    private boolean questionEnt14;
    private boolean questionEnt15;
    private boolean questionEnt16;
    private boolean questionEnt17;
    private boolean questionEnt18;
    private boolean questionEnt19;
    private boolean questionEnt2;
    private boolean questionEnt3;
    private boolean questionEnt4;
    private boolean questionEnt5;
    private boolean questionEnt6;
    private boolean questionEnt7;
    private boolean questionEnt8;
    private boolean questionEnt9;
    private boolean questionEtuI1;
    private boolean questionEtuI2;
    private boolean questionEtuI3;
    private boolean questionEtuI4;
    private boolean questionEtuI5;
    private boolean questionEtuI6;
    private boolean questionEtuI7;
    private boolean questionEtuI8;
    private boolean questionEtuII1;
    private boolean questionEtuII2;
    private boolean questionEtuII3;
    private boolean questionEtuII4;
    private boolean questionEtuII5;
    private boolean questionEtuII6;
    private boolean questionEtuIII1;
    private boolean questionEtuIII10;
    private boolean questionEtuIII11;
    private boolean questionEtuIII12;
    private boolean questionEtuIII13;
    private boolean questionEtuIII14;
    private boolean questionEtuIII15;
    private boolean questionEtuIII16;
    private boolean questionEtuIII2;
    private boolean questionEtuIII3;
    private boolean questionEtuIII4;
    private boolean questionEtuIII5;
    private boolean questionEtuIII6;
    private boolean questionEtuIII7;
    private boolean questionEtuIII8;
    private boolean questionEtuIII9;
    private boolean validationEnseignant;
    private boolean validationEntreprise;
    private boolean validationEtudiant;
    // bi-directional many-to-one association to CentreGestion
    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    private CentreGestion centreGestion;
    // bi-directional many-to-one association to QuestionSupplementaire
    @OneToMany(mappedBy = "ficheEvaluation")
    private List<QuestionSupplementaire> questionSupplementaires;
    // bi-directional many-to-one association to ReponseEvaluation
    @OneToMany(mappedBy = "ficheEvaluation")
    private List<ReponseEvaluation> reponseEvaluations;


    public QuestionSupplementaire addQuestionSupplementaire(QuestionSupplementaire questionSupplementaire) {
        getQuestionSupplementaires().add(questionSupplementaire);
        questionSupplementaire.setFicheEvaluation(this);
        return questionSupplementaire;
    }

    public QuestionSupplementaire removeQuestionSupplementaire(QuestionSupplementaire questionSupplementaire) {
        getQuestionSupplementaires().remove(questionSupplementaire);
        questionSupplementaire.setFicheEvaluation(null);
        return questionSupplementaire;
    }


    public ReponseEvaluation addReponseEvaluation(ReponseEvaluation reponseEvaluation) {
        getReponseEvaluations().add(reponseEvaluation);
        reponseEvaluation.setFicheEvaluation(this);
        return reponseEvaluation;
    }

    public ReponseEvaluation removeReponseEvaluation(ReponseEvaluation reponseEvaluation) {
        getReponseEvaluations().remove(reponseEvaluation);
        reponseEvaluation.setFicheEvaluation(null);
        return reponseEvaluation;
    }

}