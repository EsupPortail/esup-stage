package org.esup_portail.esup_stage.dto;

public class LdapSearchDto {
    private String id;
    private String nom;
    private String prenom;
    private String mail;
    private String primaryAffiliation;
    private String affiliation;
    private String supannAliasLogin;
    private String codEtu;
    private String supannEtuEtape;
    private String supannEntiteAffectation;
    private String supannEtuAnneeInscription;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPrimaryAffiliation() {
        return primaryAffiliation;
    }

    public void setPrimaryAffiliation(String primaryAffiliation) {
        this.primaryAffiliation = primaryAffiliation;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getSupannAliasLogin() {
        return supannAliasLogin;
    }

    public void setSupannAliasLogin(String supannAliasLogin) {
        this.supannAliasLogin = supannAliasLogin;
    }

    public String getCodEtu() {
        return codEtu;
    }

    public void setCodEtu(String codEtu) {
        this.codEtu = codEtu;
    }

    public String getSupannEtuEtape() {
        return supannEtuEtape;
    }

    public void setSupannEtuEtape(String supannEtuEtape) {
        this.supannEtuEtape = supannEtuEtape;
    }

    public String getSupannEntiteAffectation() {
        return supannEntiteAffectation;
    }

    public void setSupannEntiteAffectation(String supannEntiteAffectation) {
        this.supannEntiteAffectation = supannEntiteAffectation;
    }

    public String getSupannEtuAnneeInscription() {
        return supannEtuAnneeInscription;
    }

    public void setSupannEtuAnneeInscription(String supannEtuAnneeInscription) {
        this.supannEtuAnneeInscription = supannEtuAnneeInscription;
    }

    @Override
    public String toString() {
        return "LdapSearchDto{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", mail='" + mail + '\'' +
                ", primaryAffiliation='" + primaryAffiliation + '\'' +
                ", affiliation='" + affiliation + '\'' +
                ", supannAliasLogin='" + supannAliasLogin + '\'' +
                ", codEtu='" + codEtu + '\'' +
                ", supannEtuEtape='" + supannEtuEtape + '\'' +
                ", supannEntiteAffectation='" + supannEntiteAffectation + '\'' +
                ", supannEtuAnneeInscription='" + supannEtuAnneeInscription + '\'' +
                '}';
    }
}
