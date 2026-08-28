package org.esup_portail.esup_stage.dto;

import lombok.Data;
import org.esup_portail.esup_stage.model.ApiToken;

/**
 * Réponse contenant la valeur en clair du token. N'est renvoyée qu'à la demande explicite
 * d'un administrateur (création, renouvellement, copie dans le presse-papier).
 */
@Data
public class ApiTokenSecretDto {
    private ApiToken apiToken;
    private String token;

    public ApiTokenSecretDto(ApiToken apiToken, String token) {
        this.apiToken = apiToken;
        this.token = token;
    }
}
