package org.esup_portail.esup_stage.dto;

import lombok.Data;

/**
 * Données saisies dans l'écran d'administration pour créer ou renommer un token d'API.
 */
@Data
public class ApiTokenFormDto {
    private String nom;
    private String nomApplication;
}
