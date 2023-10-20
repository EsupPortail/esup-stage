package org.esup_portail.esup_stage.service.apogee.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EtudiantDiplomeEtapeResponse {
    private String codEtu;
    private String nom;
    private String prenom;
    private String dateNaissance;
    private String numeroIne;
    private String mail;
    private String codeComposante;
    private String libelleComposante;
    private String codeDiplome;
    private String versionDiplome;
    private String codeEtape;
    private String versionEtape;
    private String libelleEtape;
    private String annee;


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

    public String getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getNumeroIne() {
        return numeroIne;
    }

    public void setNumeroIne(String numeroIne) {
        this.numeroIne = numeroIne;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getCodeComposante() {
        return codeComposante;
    }

    public void setCodeComposante(String codeComposante) {
        this.codeComposante = codeComposante;
    }

    public String getLibelleComposante() {
        return libelleComposante;
    }

    public void setLibelleComposante(String libelleComposante) {
        this.libelleComposante = libelleComposante;
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

    public String getLibelleEtape() {
        return libelleEtape;
    }

    public void setLibelleEtape(String libelleEtape) {
        this.libelleEtape = libelleEtape;
    }

    public String getAnnee() {
        return annee;
    }

    public void setAnnee(String annee) {
        this.annee = annee;
    }
}
