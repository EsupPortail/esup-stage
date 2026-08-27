package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "QuestionSupplementaire")
@Data
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

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String typeQuestion;

}
