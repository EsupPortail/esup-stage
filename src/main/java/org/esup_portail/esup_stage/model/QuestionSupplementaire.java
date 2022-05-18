package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "QuestionSupplementaire")
public class QuestionSupplementaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idQuestionSupplementaire", nullable = false)
    private int id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "idFicheEvaluation", nullable = false)
    private FicheEvaluation ficheEvaluation;

    @Column(nullable = false)
    private int idPlacement;

    @Column(nullable = false, length = 200)
    private String question;

    @Column(nullable = false, length = 5)
    private String typeQuestion;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public FicheEvaluation getFicheEvaluation() {
        return ficheEvaluation;
    }

    public void setFicheEvaluation(FicheEvaluation ficheEvaluation) {
        this.ficheEvaluation = ficheEvaluation;
    }

    public int getIdPlacement() {
        return idPlacement;
    }

    public void setIdPlacement(int idPlacement) {
        this.idPlacement = idPlacement;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getTypeQuestion() {
        return typeQuestion;
    }

    public void setTypeQuestion(String typeQuestion) {
        this.typeQuestion = typeQuestion;
    }
}
