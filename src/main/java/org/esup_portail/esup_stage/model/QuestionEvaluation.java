package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;
import lombok.Data;
import org.esup_portail.esup_stage.enums.TypeQuestionEvaluation;

@Entity
@Table(name = "QuestionEvaluation")
@Data
public class QuestionEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idQuestionEvaluation", nullable = false)
    private Integer id;

    @Column(name = "codeQuestionEvaluation", nullable = false)
    private String code;

    @Column(name = "texteQuestionEvaluation", nullable = false)
    private String texte;

    @Enumerated(EnumType.STRING)
    @Column(name = "typeQuestionEvaluation", nullable = false, length = 32)
    private TypeQuestionEvaluation type;

    @Column(name = "paramsJson", columnDefinition = "TEXT")
    private String paramsJson;

    @Column(name="questionBis")
    private String questionBis;
}
