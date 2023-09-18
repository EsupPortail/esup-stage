package org.esup_portail.esup_stage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.logging.log4j.util.Strings;
import org.esup_portail.esup_stage.enums.SignataireEnum;

import javax.validation.constraints.NotNull;

public class MetadataDto {

    @NotNull
    private SignataireEnum signataire;

    @NotNull
    private String nom;

    @NotNull
    private String prenom;

    @NotNull
    private String email;

    private String telephone;

    public SignataireEnum getSignataire() {
        return signataire;
    }

    public void setSignataire(SignataireEnum signataire) {
        this.signataire = signataire;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return Strings.isEmpty(telephone) ? null : telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
