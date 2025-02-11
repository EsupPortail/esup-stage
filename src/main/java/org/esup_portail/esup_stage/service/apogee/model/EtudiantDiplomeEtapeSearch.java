package org.esup_portail.esup_stage.service.apogee.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EtudiantDiplomeEtapeSearch {

    @NotNull
    private String codeComposante;

    @NotNull
    private String annee;

    @NotNull
    private String codeEtape;

    @NotNull
    private String versionEtape;

    @NotNull
    private String codeDiplome;

    @NotNull
    private String versionDiplome;

    private String codEtu;
    private String nom;
    private String prenom;

    public String getCodeComposante() {
        return codeComposante;
    }

    public void setCodeComposante(String codeComposante) {
        this.codeComposante = codeComposante;
    }

    public String getAnnee() {
        return annee;
    }

    public void setAnnee(String annee) {
        this.annee = annee;
    }

    public String getCodeEtape() {
        return codeEtape;
    }

    public void setCodeEtape(String codeEtape) {
        this.codeEtape = codeEtape;
    }

    public String getVersionEtape() {
        return versionEtape;
    }

    public void setVersionEtape(String versionEtape) {
        this.versionEtape = versionEtape;
    }

    public String getCodeDiplome() {
        return codeDiplome;
    }

    public void setCodeDiplome(String codeDiplome) {
        this.codeDiplome = codeDiplome;
    }

    public String getVersionDiplome() {
        return versionDiplome;
    }

    public void setVersionDiplome(String versionDiplome) {
        this.versionDiplome = versionDiplome;
    }

    public String getCodEtu() {
        return codEtu;
    }

    public void setCodEtu(String codEtu) {
        this.codEtu = codEtu;
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
}
