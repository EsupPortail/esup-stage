package org.esup_portail.esup_stage.dto;

import lombok.Data;

@Data
public class ContactDto {
    private int id;
    private String nom;
    private String prenom;
    private int idCentreGestion;
}
