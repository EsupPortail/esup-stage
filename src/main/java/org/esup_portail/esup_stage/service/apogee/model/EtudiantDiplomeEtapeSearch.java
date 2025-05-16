package org.esup_portail.esup_stage.service.apogee.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EtudiantDiplomeEtapeSearch {

    @NotNull
    private String annee;

    @NotNull
    private String codeEtape;

    @NotNull
    private String versionEtape;

    @NotNull
    private String codeDiplome;

    @NotNull
    private String versionDiplome;

    private String codEtu;
    private String nom;
    private String prenom;

}
