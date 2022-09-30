package org.esup_portail.esup_stage.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class TemplateMailFormDto implements TemplateMailInterface {

    @NotNull
    @NotEmpty
    @Size(max = 500)
    private String libelle;

    @NotNull
    @NotEmpty
    @Size(max = 250)
    private String objet;

    @NotNull
    @NotEmpty
    private String texte;

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }
}
