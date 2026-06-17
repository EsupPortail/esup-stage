package org.esup_portail.esup_stage.dto;

import lombok.Data;
import org.esup_portail.esup_stage.model.Pays;

@Data
public class ServiceDto {
    private int id;
    private String nom;
    private String voie;
    private String codePostal;
    private String batimentResidence;
    private String commune;
    private String telephone;
    private Pays pays;
    private int idCentreGestion;
}
