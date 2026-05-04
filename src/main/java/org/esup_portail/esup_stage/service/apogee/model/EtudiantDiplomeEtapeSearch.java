package org.esup_portail.esup_stage.service.apogee.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EtudiantDiplomeEtapeSearch {

    @NotBlank
    private String annee;

    @NotBlank
    private String codeComposante;

    @NotBlank
    private String codeEtape;

    @NotBlank
    private String versionEtape;

    @NotBlank
    private String codeDiplome;

    @NotBlank
    private String versionDiplome;

    private String codEtu;
    private String nom;
    private String prenom;

}
