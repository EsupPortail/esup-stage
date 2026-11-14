package org.esup_portail.esup_stage.dto;

import lombok.Data;

/**
 * Projection légère des templates de convention (sans texte / texteAvenant),
 * destinée aux écrans centre de gestion (ex. aperçu logo).
 */
@Data
public class TemplateConventionLiteDto {

    private int id;
    private TypeConventionLiteDto typeConvention;
    private LangueConventionLiteDto langueConvention;

    public TemplateConventionLiteDto(int id, String typeConventionLibelle, String langueConventionLibelle) {
        this.id = id;
        this.typeConvention = new TypeConventionLiteDto(typeConventionLibelle);
        this.langueConvention = new LangueConventionLiteDto(langueConventionLibelle);
    }

    @Data
    public static class TypeConventionLiteDto {
        private String libelle;

        public TypeConventionLiteDto(String libelle) {
            this.libelle = libelle;
        }
    }

    @Data
    public static class LangueConventionLiteDto {
        private String libelle;

        public LangueConventionLiteDto(String libelle) {
            this.libelle = libelle;
        }
    }
}
