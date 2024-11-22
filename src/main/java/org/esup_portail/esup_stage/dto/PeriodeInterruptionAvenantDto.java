package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Date;

public class PeriodeInterruptionAvenantDto {

    private Date dateDebutInterruption;

    private Date dateFinInterruption;

    @NotNull
    private Boolean isModif;

    private int idPeriodeInterruptionStage;

    private int idAvenant;

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

    public Boolean getIsModif() {
        return isModif;
    }

    public void setIsModif(Boolean modif) {
        isModif = modif;
    }

    public int getIdPeriodeInterruptionStage() {
        return idPeriodeInterruptionStage;
    }

    public void setIdPeriodeInterruptionStage(int idPeriodeInterruptionStage) {
        this.idPeriodeInterruptionStage = idPeriodeInterruptionStage;
    }

    public int getIdAvenant() {
        return idAvenant;
    }

    public void setIdAvenant(int idAvenant) {
        this.idAvenant = idAvenant;
    }
}