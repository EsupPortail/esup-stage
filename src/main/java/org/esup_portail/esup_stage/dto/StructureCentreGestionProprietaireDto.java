package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StructureCentreGestionProprietaireDto {

    @NotNull
    private Integer idCentreGestionProprietaire;
}
