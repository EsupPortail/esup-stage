package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.esup_portail.esup_stage.constants.ValidationPatterns;

@Data
public class ContactFormDto {

    @NotNull
    @NotEmpty
    @Size(max = 50)
    private String nom;

    @NotNull
    @NotEmpty
    @Size(max = 50)
    private String prenom;

    @NotNull
    @NotEmpty
    @Pattern(regexp = ValidationPatterns.EMAIL, message = "L'adresse mail n'est pas valide")
    @Size(max = 255)
    private String mail;

    @NotNull
    @NotEmpty
    @Size(max = 50)
    private String tel;

    @Size(max = 50)
    private String fax;

    private int idCivilite;

    @NotNull
    @NotEmpty
    @Size(max = 100)
    private String fonction;

    @NotNull
    private int idService;

    private Integer idCentreGestion;

    private Boolean refusEtreContacte;
}
