package org.esup_portail.esup_stage.enums;

public enum FolderEnum {
    CENTRE_GESTION_CONSIGNE_DOCS("/centregestion/consigne-documents"),
    CENTRE_GESTION_LOGOS("/centregestion/logos"),
    IMAGES("/images"),
    SIGNATURES("/signatures");

    private String value = "";

    FolderEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
