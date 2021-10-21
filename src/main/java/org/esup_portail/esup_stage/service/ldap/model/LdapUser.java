package org.esup_portail.esup_stage.service.ldap.model;

import java.util.ArrayList;
import java.util.List;

public class LdapUser {
    private String uid;
    private String codEtu;
    private List<String> sn = new ArrayList<>();
    private String mail;
    private String supannAutreMail;
    private String cn;
    private List<String> givenName = new ArrayList<>();
    private String displayName;
    private String eduPersonPrimaryAffiliation;
    private List<String> eduPersonAffiliation = new ArrayList<>();

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCodEtu() {
        return codEtu;
    }

    public void setCodEtu(String codEtu) {
        this.codEtu = codEtu;
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
}
