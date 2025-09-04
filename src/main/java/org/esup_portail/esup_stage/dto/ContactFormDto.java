package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

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
    @Email
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

}
