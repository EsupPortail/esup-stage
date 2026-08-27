package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.esup_portail.esup_stage.constants.ValidationPatterns;

@Data
public class EnseignantDto {

    @NotNull
    @NotEmpty
    @Size(max = 50)
    private String nom;

    @NotNull
    @NotEmpty
    @Size(max = 50)
    private String prenom;

    @Pattern(regexp = ValidationPatterns.EMAIL, message = "L'adresse mail n'est pas valide")
    @Size(max = 255)
    private String mail;

    @Size(max = 30)
    private String tel;

    @Size(max = 50)
    private String fax;

    @Size(max = 50)
    private String typePersonne;

    @NotNull
    @NotEmpty
    @Size(max = 50)
    private String uidEnseignant;

    @Size(max = 250)
    private String campus;

    @Size(max = 20)
    private String bureau;

    @Size(max = 45)
    private String batiment;

}