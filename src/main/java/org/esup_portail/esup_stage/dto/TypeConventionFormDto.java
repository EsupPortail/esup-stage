package org.esup_portail.esup_stage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TypeConventionFormDto {

    private String libelle;

    private String temEnServ;

    private List<RegimeInscriptionDto> regimesInscription;

    private List<RegimeInscriptionDto> typeInscription;
}
