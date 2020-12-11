package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the QuestionSupplementaire database table.
 *
 */
@Entity
@Table(name = "QuestionSupplementaire")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "QuestionSupplementaire.findAll", query = "SELECT q FROM QuestionSupplementaire q")
public class QuestionSupplementaire implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idQuestionSupplementaire;
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