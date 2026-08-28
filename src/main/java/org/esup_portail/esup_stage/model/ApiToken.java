package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

/**
 * Token d'accès à l'API publique (/public), à raison d'un token par application appelante.
 * La valeur est stockée chiffrée (AES/GCM) avec la même clé que la configuration en base
 * ({@code appli.configEncryptionKey}), et n'est jamais exposée dans les listes.
 */
@Entity
@Table(name = "ApiToken")
@Data
public class ApiToken implements Exportable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    private String nomApplication;

    // Jamais sérialisé : PaginationRepository renvoie l'entité telle quelle dans la réponse paginée
    @JsonIgnore
    @Column(nullable = false, columnDefinition = "text")
    private String tokenEncrypted;

    @Column(nullable = false)
    private boolean actif = true;

    @Column
    private Date dateCreation;

    @Column
    private String loginCreation;

    @Column
    private Date dateModification;

    @Column
    private String loginModification;

    @Override
    public String getExportValue(String key) {
        return switch (key) {
            case "id" -> id != null ? id.toString() : "";
            case "nom" -> nom != null ? nom : "";
            case "nomApplication" -> nomApplication != null ? nomApplication : "";
            case "actif" -> Boolean.toString(actif);
            case "dateCreation" -> dateCreation != null ? dateCreation.toString() : "";
            case "loginCreation" -> loginCreation != null ? loginCreation : "";
            case "dateModification" -> dateModification != null ? dateModification.toString() : "";
            case "loginModification" -> loginModification != null ? loginModification : "";
            default -> "";
        };
    }
}
