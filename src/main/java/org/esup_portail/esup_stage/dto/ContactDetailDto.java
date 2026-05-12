package org.esup_portail.esup_stage.dto;

import lombok.Data;
import org.esup_portail.esup_stage.model.Civilite;

@Data
public class ContactDetailDto {
    private int id;
    private String nom;
    private String prenom;
    private String mail;
    private String tel;
    private String fonction;
    private Civilite civilite;
    private String fax;
    private int idCentreGestion;
}
