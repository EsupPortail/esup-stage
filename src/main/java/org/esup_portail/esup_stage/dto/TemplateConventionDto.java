package org.esup_portail.esup_stage.dto;

import org.esup_portail.esup_stage.model.LangueConvention;
import org.esup_portail.esup_stage.model.TypeConvention;

import java.util.ArrayList;
import java.util.List;

public class TemplateConventionDto {

    private String libelle;
    private String texte;
    private String texteAvenant;
    private TypeConvention typeConvention;
    private List<TypeConvention> typeConventions = new ArrayList<>();
    private LangueConvention langueConvention;

    public TemplateConventionDto() {
    }

    public TemplateConventionDto(String texte, String texteAvenant) {
        this.texte = texte;
        this.texteAvenant = texteAvenant;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
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

    public TypeConvention getTypeConvention() {
        return typeConvention;
    }

    public void setTypeConvention(TypeConvention typeConvention) {
        this.typeConvention = typeConvention;
    }

    public List<TypeConvention> getTypeConventions() {
        return typeConventions;
    }

    public void setTypeConventions(List<TypeConvention> typeConventions) {
        this.typeConventions = typeConventions;
    }

    public LangueConvention getLangueConvention() {
        return langueConvention;
    }

    public void setLangueConvention(LangueConvention langueConvention) {
        this.langueConvention = langueConvention;
    }
}
