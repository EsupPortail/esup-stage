package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Date;

public class PeriodeInterruptionStageDto {

    @NotNull
    private Date dateDebutInterruption;

    @NotNull
    private Date dateFinInterruption;

    @NotNull
    private int idConvention;

    public Date getDateDebutInterruption() {
        return dateDebutInterruption;
    }

    public void setDateDebutInterruption(Date dateDebutInterruption) {
        this.dateDebutInterruption = dateDebutInterruption;
    }

    public Date getDateFinInterruption() {
        return dateFinInterruption;
    }

    public void setDateFinInterruption(Date dateFinInterruption) {
        this.dateFinInterruption = dateFinInterruption;
    }

    public int getIdConvention() {
        return idConvention;
    }

    public void setIdConvention(int idConvention) {
        this.idConvention = idConvention;
    }
}