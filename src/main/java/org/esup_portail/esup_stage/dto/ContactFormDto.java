package org.esup_portail.esup_stage.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ContactFormDto {

    @NotNull
    @NotEmpty
    @Size(max = 50)
    private String nom;

    @NotNull
    @NotEmpty
    @Size(max = 50)
    private String prenom;

    @Email
    @Size(max = 50)
    private String mail;

    @Size(max = 50)
    private String tel;

    @Size(max = 50)
    private String fax;

    @NotNull
    private int idCivilite;

    @NotNull
    @NotEmpty
    @Size(max = 100)
    private String fonction;

    @NotNull
    private int idService;

    @NotNull
    private int idCentreGestion;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public int getIdCivilite() {
        return idCivilite;
    }

    public void setIdCivilite(int idCivilite) {
        this.idCivilite = idCivilite;
    }

    public String getFonction() {
        return fonction;
    }

    public void setFonction(String fonction) {
        this.fonction = fonction;
    }

    public int getIdService() {
        return idService;
    }

    public void setIdService(int idService) {
        this.idService = idService;
    }

    public int getIdCentreGestion() {
        return idCentreGestion;
    }

    public void setIdCentreGestion(int idCentreGestion) {
        this.idCentreGestion = idCentreGestion;
    }
}
