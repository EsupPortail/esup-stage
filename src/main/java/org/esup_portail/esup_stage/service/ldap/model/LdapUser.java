package org.esup_portail.esup_stage.service.ldap.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LdapUser {
    private String uid;
    private String supannAliasLogin;
    private String codEtu;
    private String supannEtuId;
    private List<String> sn = new ArrayList<>();
    private String mail;
    private String supannAutreMail;
    private String cn;
    private List<String> givenName = new ArrayList<>();
    private String displayName;
    private String eduPersonPrimaryAffiliation;
    private List<String> eduPersonAffiliation = new ArrayList<>();
    private List<String> supannEntiteAffectation = new ArrayList<>();
    private String supannEtuAnneeInscription;
    private List<String> supannEtuEtape = new ArrayList<>();
    private String supannEntiteAffectationPrincipale;
    private String supannCivilite;
    private String telephoneNumber;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getSupannEtuId() {
        return supannEtuId;
    }

    public void setSupannEtuId(String supannEtuId) {
        this.supannEtuId = supannEtuId;
    }

    public List<String> getSn() {
        return sn;
    }

    public void setSn(List<String> sn) {
        this.sn = sn;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getSupannAutreMail() {
        return supannAutreMail;
    }

    public void setSupannAutreMail(String supannAutreMail) {
        this.supannAutreMail = supannAutreMail;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public List<String> getGivenName() {
        return givenName;
    }

    public void setGivenName(List<String> givenName) {
        this.givenName = givenName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEduPersonPrimaryAffiliation() {
        return eduPersonPrimaryAffiliation;
    }

    public void setEduPersonPrimaryAffiliation(String eduPersonPrimaryAffiliation) {
        this.eduPersonPrimaryAffiliation = eduPersonPrimaryAffiliation;
    }

    public List<String> getEduPersonAffiliation() {
        return eduPersonAffiliation;
    }

    public void setEduPersonAffiliation(List<String> eduPersonAffiliation) {
        this.eduPersonAffiliation = eduPersonAffiliation;
    }

    public List<String> getSupannEntiteAffectation() {
        return supannEntiteAffectation;
    }

    public void setSupannEntiteAffectation(List<String> supannEntiteAffectation) {
        this.supannEntiteAffectation = supannEntiteAffectation;
    }

    public String getSupannEtuAnneeInscription() {
        return supannEtuAnneeInscription;
    }

    public void setSupannEtuAnneeInscription(String supannEtuAnneeInscription) {
        this.supannEtuAnneeInscription = supannEtuAnneeInscription;
    }

    public List<String> getSupannEtuEtape() {
        return supannEtuEtape;
    }

    public void setSupannEtuEtape(List<String> supannEtuEtape) {
        this.supannEtuEtape = supannEtuEtape;
    }

    public String getSupannEntiteAffectationPrincipale() {
        return supannEntiteAffectationPrincipale;
    }

    public void setSupannEntiteAffectationPrincipale(String supannEntiteAffectationPrincipale) {
        this.supannEntiteAffectationPrincipale = supannEntiteAffectationPrincipale;
    }

    public String getSupannCivilite() {
        return supannCivilite;
    }

    public void setSupannCivilite(String supannCivilite) {
        this.supannCivilite = supannCivilite;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }
}
