package org.esup_portail.esup_stage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineErrorDto {
    private int line;       // numéro de ligne (1-based)
    private String field;   // ex: "SIRET", "RNE", "Email"
    private String message; // ex: "SIRET invalide (14 chiffres attendus)"
    private String value;
}