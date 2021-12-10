package org.esup_portail.esup_stage.dto;

public class TemplateConventionDto {

    private String texte;
    private String texteAvenant;

    public TemplateConventionDto() {
    }

    public TemplateConventionDto(String texte, String texteAvenant) {
        this.texte = texte;
        this.texteAvenant = texteAvenant;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public String getTexteAvenant() {
        return texteAvenant;
    }

    public void setTexteAvenant(String texteAvenant) {
        this.texteAvenant = texteAvenant;
    }
}
