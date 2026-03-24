package org.esup_portail.esup_stage.service.apogee.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RegimeInscription {
    private String regimeIns;
    private String libRg;
    private String annee;
    private String codRegIns;
    private String licRegIns;

    @JsonAlias({"codSisRegIns", "codSISRegIns", "codSISRGI", "COD_SIS_RGI", "cod_sis_rgi"})
    private String codSisRegIns;
}