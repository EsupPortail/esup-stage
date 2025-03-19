package org.esup_portail.esup_stage.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PeriodeStageDto {

    private Date dateDebut;
    private Date dateFin;
    private Integer nbHeuresJournalieres;
    private Integer idConvention;

    public PeriodeStageDto() {
    }

    public PeriodeStageDto(Date dateDebut, Date dateFin, Integer nbHeuresJournalieres, Integer idConvention) {
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.nbHeuresJournalieres = nbHeuresJournalieres;
        this.idConvention = idConvention;
    }

}
