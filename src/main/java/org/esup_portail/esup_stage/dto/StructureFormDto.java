package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.esup_portail.esup_stage.constants.ValidationPatterns;
import org.hibernate.validator.constraints.URL;

@Data
public class StructureFormDto {

    @Size(max = 20)
    private String libCedex;

    @Size(min = 14, max = 14)
    private String numeroSiret;

    @Size(max = 6)
    private String codeNafN5;

    @NotNull
    @NotEmpty
    @Size(max = 150)
    private String raisonSociale;

    private String activitePrincipale;

    @Size(max = 20)
    private String telephone;

    @Size(max = 20)
    private String fax;

    @Pattern(regexp = ValidationPatterns.EMAIL, message = "L'adresse mail n'est pas valide")
    @Size(max = 255)
    private String mail;

    @URL
    @Size(max = 200)
    private String siteWeb;

    @NotNull
    private int idEffectif;

    @NotNull
    private int idStatutJuridique;

    @NotNull
    private int idTypeStructure;

    @Size(max = 200)
    private String batimentResidence;

    @NotNull
    @NotEmpty
    @Size(max = 200)
    private String voie;

    @NotNull
    @NotEmpty
    @Size(max = 200)
    private String commune;

    @NotNull
    @Size(max = 10)
    private String codePostal;

    @NotNull
    private int idPays;

    @Size(max = 20)
    private String numeroRNE;

    private Boolean verrouillageSynchroStructureSirene;

    private Boolean confidentialiteCoordonnees;

    private Integer idCentreGestionProprietaire;
}