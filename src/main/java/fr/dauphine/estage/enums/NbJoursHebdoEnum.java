package fr.dauphine.estage.enums;

public enum NbJoursHebdoEnum {
    ZERO_CINQ("0.5"),
    UN("1"),
    UN_CINQ("1.5"),
    DEUX("2"),
    DEUX_CINQ("2.5"),
    TROIS("3"),
    TROIS_CINQ("3.5"),
    QUATRE("4"),
    QUATRE_CINQ("4.5"),
    CINQ("5"),
    CINQ_CINQ("5.5"),
    SIX("6");

    private String value = "";

    NbJoursHebdoEnum(String value) {
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
