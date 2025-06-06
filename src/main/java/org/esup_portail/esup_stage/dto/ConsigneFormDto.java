package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotNull;

public class ConsigneFormDto {
    @NotNull
    private String texte;

    @NotNull
    private int idCentreGestion;

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public int getIdCentreGestion() {
        return idCentreGestion;
    }

    public void setIdCentreGestion(int idCentreGestion) {
        this.idCentreGestion = idCentreGestion;
    }
}
