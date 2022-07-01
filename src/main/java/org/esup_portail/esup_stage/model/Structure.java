package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.view.Views;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Structure")
public class Structure extends ObjetMetier implements Exportable {

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idStructure", nullable = false)
    private int id;

    @Column
    private String libCedex;

    @Column
    private String codeEtab;

    @JsonView(Views.List.class)
    @Column
    private String numeroSiret;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumn(name = "codeNAF_N5")
    private NafN5 nafN5;

    @JsonView(Views.List.class)
    @Column(nullable = false)
    private String raisonSociale;

    @Lob
    private String activitePrincipale;

    @Column
    private String telephone;

    @Column
    private String fax;

    @Column
    private String mail;

    @Column
    private String siteWeb;

    @Column
    private String groupe;

    @Column
    private String logo;

    @Column(nullable = false)
    private boolean estValidee;

    @Column
    private String loginValidation;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateValidation;

    @Column
    private String loginStopValidation;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateStopValidation;

    @Temporal(TemporalType.DATE)
    private Date infosAJour;

    @Column
    private String loginInfosAJour;

    @ManyToOne
    @JoinColumn(name = "idEffectif", nullable = false)
    private Effectif effectif;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumn(name = "idStatutJuridique")
    private StatutJuridique statutJuridique;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumn(name = "idTypeStructure", nullable = false)
    private TypeStructure typeStructure;

    @Column
    private String nomDirigeant;

    @Column
    private String prenomDirigeant;

    @Column(length = 1)
    private String temEnServStructure;

    @Column
    private String batimentResidence;

    @Column(nullable = false)
    private String voie;

    @JsonView(Views.List.class)
    @Column
    private String commune;

    @Column
    private String codePostal;

    @Column
    private String codeCommune;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumn(name = "idPays", nullable = false)
    private Pays pays;

    @Column(length = 8)
    private String numeroRNE;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLibCedex() {
        return libCedex;
    }

    public void setLibCedex(String libCedex) {
        this.libCedex = libCedex;
    }

    public String getCodeEtab() {
        return codeEtab;
    }

    public void setCodeEtab(String codeEtab) {
        this.codeEtab = codeEtab;
    }

    public String getNumeroSiret() {
        return numeroSiret;
    }

    public void setNumeroSiret(String numeroSiret) {
        this.numeroSiret = numeroSiret;
    }

    public NafN5 getNafN5() {
        return nafN5;
    }

    public void setNafN5(NafN5 nafN5) {
        this.nafN5 = nafN5;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public String getActivitePrincipale() {
        return activitePrincipale;
    }

    public void setActivitePrincipale(String activitePrincipale) {
        this.activitePrincipale = activitePrincipale;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

    public String getGroupe() {
        return groupe;
    }

    public void setGroupe(String groupe) {
        this.groupe = groupe;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public boolean isEstValidee() {
        return estValidee;
    }

    public void setEstValidee(boolean estValidee) {
        this.estValidee = estValidee;
    }

    public String getLoginValidation() {
        return loginValidation;
    }

    public void setLoginValidation(String loginValidation) {
        this.loginValidation = loginValidation;
    }

    public Date getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(Date dateValidation) {
        this.dateValidation = dateValidation;
    }

    public String getLoginStopValidation() {
        return loginStopValidation;
    }

    public void setLoginStopValidation(String loginStopValidation) {
        this.loginStopValidation = loginStopValidation;
    }

    public Date getDateStopValidation() {
        return dateStopValidation;
    }

    public void setDateStopValidation(Date dateStopValidation) {
        this.dateStopValidation = dateStopValidation;
    }

    public Date getInfosAJour() {
        return infosAJour;
    }

    public void setInfosAJour(Date infosAJour) {
        this.infosAJour = infosAJour;
    }

    public String getLoginInfosAJour() {
        return loginInfosAJour;
    }

    public void setLoginInfosAJour(String loginInfosAJour) {
        this.loginInfosAJour = loginInfosAJour;
    }

    public Effectif getEffectif() {
        return effectif;
    }

    public void setEffectif(Effectif effectif) {
        this.effectif = effectif;
    }

    public StatutJuridique getStatutJuridique() {
        return statutJuridique;
    }

    public void setStatutJuridique(StatutJuridique statutJuridique) {
        this.statutJuridique = statutJuridique;
    }

    public TypeStructure getTypeStructure() {
        return typeStructure;
    }

    public void setTypeStructure(TypeStructure typeStructure) {
        this.typeStructure = typeStructure;
    }

    public String getNomDirigeant() {
        return nomDirigeant;
    }

    public void setNomDirigeant(String nomDirigeant) {
        this.nomDirigeant = nomDirigeant;
    }

    public String getPrenomDirigeant() {
        return prenomDirigeant;
    }

    public void setPrenomDirigeant(String prenomDirigeant) {
        this.prenomDirigeant = prenomDirigeant;
    }

    public String getTemEnServStructure() {
        return temEnServStructure;
    }

    public void setTemEnServStructure(String temEnServStructure) {
        this.temEnServStructure = temEnServStructure;
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

    public String getCodeCommune() {
        return codeCommune;
    }

    public void setCodeCommune(String codeCommune) {
        this.codeCommune = codeCommune;
    }

    public Pays getPays() {
        return pays;
    }

    public void setPays(Pays pays) {
        this.pays = pays;
    }

    public String getNumeroRNE() {
        return numeroRNE;
    }

    public void setNumeroRNE(String numeroRNE) {
        this.numeroRNE = numeroRNE;
    }

    @Override
    public String getExportValue(String key) {
        String value = "";
        switch (key) {
            case "raisonSociale":
                value = getRaisonSociale();
                break;
            case "numeroSiret":
                value = getNumeroSiret();
                break;
            case "nafN5":
                if (getNafN5() != null) {
                    value = getNafN5().getLibelle();
                }
                break;
            case "pays":
                if (getPays() != null) {
                    value = getPays().getLib();
                }
                break;
            case "commune":
                value = getCommune();
                break;
            case "typeStructure":
                if (getTypeStructure() != null) {
                    value = getTypeStructure().getLibelle();
                }
                break;
            case "statutJuridique":
                if (getStatutJuridique() != null) {
                    value = getStatutJuridique().getLibelle();
                }
                break;
            default:
                break;
        }
        return value;
    }
}
