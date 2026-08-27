package org.esup_portail.esup_stage.dto;

import lombok.Data;

@Data
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
