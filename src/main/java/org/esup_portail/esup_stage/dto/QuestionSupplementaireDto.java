package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionSupplementaireDto {

    @NotNull
    private int idPlacement;

    @NotNull
    private String question;

    @NotNull
    private String typeQuestion;

}