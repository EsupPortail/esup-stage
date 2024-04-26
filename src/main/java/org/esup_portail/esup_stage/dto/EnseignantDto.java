package org.esup_portail.esup_stage.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class EnseignantDto {

    @NotNull
    @NotEmpty
    @Size(max = 50)
    private String nom;

    @NotNull
    @NotEmpty
    @Size(max = 50)
    private String prenom;

    @Email
    @Size(max = 255)
    private String mail;

    @Size(max = 30)
    private String tel;

    @Size(max = 50)
    private String fax;

    @Size(max = 50)
    private String typePersonne;

    @NotNull
    @NotEmpty
    @Size(max = 50)
    private String uidEnseignant;

    @Size(max = 250)
    private String campus;

    @Size(max = 20)
    private String bureau;

    @Size(max = 45)
    private String batiment;


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

    public String getTypePersonne() {
        return typePersonne;
    }

    public void setTypePersonne(String typePersonne) {
        this.typePersonne = typePersonne;
    }

    public String getUidEnseignant() {
        return uidEnseignant;
    }

    public void setUidEnseignant(String uidEnseignant) {
        this.uidEnseignant = uidEnseignant;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getBureau() {
        return bureau;
    }

    public void setBureau(String bureau) {
        this.bureau = bureau;
    }

    public String getBatiment() {
        return batiment;
    }

    public void setBatiment(String batiment) {
        this.batiment = batiment;
    }

}