package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ServiceFormDto {

    @NotNull
    @NotEmpty
    @Size(max = 150)
    private String nom;

    @NotNull
    private int idStructure;

    @Size(max = 20)
    private String telephone;

    @Size(max = 200)
    private String batimentResidence;

    @NotNull
    @NotEmpty
    @Size(max = 200)
    private String voie;

    @NotNull
    @NotEmpty
    @Size(max = 200)
    private String commune;

    @NotNull
    @NotEmpty
    @Size(max = 10)
    private String codePostal;

    @NotNull
    private int idPays;

    private Integer idCentreGestion;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getIdStructure() {
        return idStructure;
    }

    public void setIdStructure(int idStructure) {
        this.idStructure = idStructure;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getBatimentResidence() {
        return batimentResidence;
    }

    public void setBatimentResidence(String batimentResidence) {
        this.batimentResidence = batimentResidence;
    }

    public String getVoie() {
        return voie;
    }

    public void setVoie(String voie) {
        this.voie = voie;
    }

    public String getCommune() {
        return commune;
    }

    public void setCommune(String commune) {
        this.commune = commune;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public int getIdPays() {
        return idPays;
    }

    public void setIdPays(int idPays) {
        this.idPays = idPays;
    }

    public Integer getIdCentreGestion() {
        return idCentreGestion;
    }

    public void setIdCentreGestion(Integer idCentreGestion) {
        this.idCentreGestion = idCentreGestion;
    }
}
