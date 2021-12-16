package org.esup_portail.esup_stage.dto;

import javax.validation.constraints.NotNull;

public class ConsigneFormDto {
    @NotNull
    private String texte;

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }
}
