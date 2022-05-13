package org.esup_portail.esup_stage.dto;
import javax.validation.constraints.NotNull;

public class QuestionSupplementaireDto {

    @NotNull
    private int idPlacement;

    @NotNull
    private String question;

    @NotNull
    private String typeQuestion;

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