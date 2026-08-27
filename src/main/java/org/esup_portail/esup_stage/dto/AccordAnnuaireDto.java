package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccordAnnuaireDto {

    @NotNull
    private Boolean accordAnnuaireEtudiant;
}
