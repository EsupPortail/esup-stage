package org.esup_portail.esup_stage.dto;

public class PaysDto {

    private int id;
    private String libelle;
    private String temEnServ;

    public PaysDto() {}

    public PaysDto(int id, String libelle, String temEnServ) {
        this.id = id;
        this.libelle = libelle;
        this.temEnServ = temEnServ;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getTemEnServ() {
        return temEnServ;
    }

    public void setTemEnServ(String temEnServ) {
        this.temEnServ = temEnServ;
    }
}
