package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConsigneFormDto {

    private String texte;

    @NotNull
    private int idCentreGestion;

}
