package org.esup_portail.esup_stage.service.apogee.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EtudiantRef {
    private Integer cod_ind;
    private String nompatro;
    private String nommarital;
    private String prenom;
    private String mail;
    private String mainAddress;
    private String postalCode;
    private String town;
    private String country;
    private String phone;
    private String portablePhone;
    private String mailPerso;
    private String codePays;
    private String libAd1;
    private String libAd2;
    private String libAd3;
    private String libAde;
    private String codeSexe;

    @JsonFormat(pattern="yyyy-MM-dd")
    private Date dateNais;

    private String libelleCPAM;
    private String theUfr;
    private String thecodeUFR;
    private String theEtape;
    private String theCodeEtape;
    private String theCodeVersionEtape;
    private Map<String, ElementPedagogique> elementPedagogiques;
    private List<ElementPedagogique> listeELPs;
    private String theCodeElp;
    private String theLibElp;
    private BigDecimal theCreditECTS;
    private Map<String, String> steps;
    private Map<String, String> studys;
    private List<EtapeInscription> listeEtapeInscriptions;
    private String theAssurance;
    private String theCaisseRegime;
    private AdministrationApogee administrationApogee;
    private List<String> listeAnneesUniv;
    private String volumeHoraireFormation;
    private List<String> anneesInscriptionFC;
    private List<String> stepsKey;
    private List<String> studysKey;

    public Integer getCod_ind() {
        return cod_ind;
    }

    public void setCod_ind(Integer cod_ind) {
        this.cod_ind = cod_ind;
    }

    public String getNompatro() {
        return nompatro;
    }

    public void setNompatro(String nompatro) {
        this.nompatro = nompatro;
    }

    public String getNommarital() {
        return nommarital;
    }

    public void setNommarital(String nommarital) {
        this.nommarital = nommarital;
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

    public String getMainAddress() {
        return mainAddress;
    }

    public void setMainAddress(String mainAddress) {
        this.mainAddress = mainAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPortablePhone() {
        return portablePhone;
    }

    public void setPortablePhone(String portablePhone) {
        this.portablePhone = portablePhone;
    }

    public String getMailPerso() {
        return mailPerso;
    }

    public void setMailPerso(String mailPerso) {
        this.mailPerso = mailPerso;
    }

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public String getLibAd1() {
        return libAd1;
    }

    public void setLibAd1(String libAd1) {
        this.libAd1 = libAd1;
    }

    public String getLibAd2() {
        return libAd2;
    }

    public void setLibAd2(String libAd2) {
        this.libAd2 = libAd2;
    }

    public String getLibAd3() {
        return libAd3;
    }

    public void setLibAd3(String libAd3) {
        this.libAd3 = libAd3;
    }

    public String getLibAde() {
        return libAde;
    }

    public void setLibAde(String libAde) {
        this.libAde = libAde;
    }

    public String getCodeSexe() {
        return codeSexe;
    }

    public void setCodeSexe(String codeSexe) {
        this.codeSexe = codeSexe;
    }

    public Date getDateNais() {
        return dateNais;
    }

    public void setDateNais(Date dateNais) {
        this.dateNais = dateNais;
    }

    public String getLibelleCPAM() {
        return libelleCPAM;
    }

    public void setLibelleCPAM(String libelleCPAM) {
        this.libelleCPAM = libelleCPAM;
    }

    public String getTheUfr() {
        return theUfr;
    }

    public void setTheUfr(String theUfr) {
        this.theUfr = theUfr;
    }

    public String getThecodeUFR() {
        return thecodeUFR;
    }

    public void setThecodeUFR(String thecodeUFR) {
        this.thecodeUFR = thecodeUFR;
    }

    public String getTheEtape() {
        return theEtape;
    }

    public void setTheEtape(String theEtape) {
        this.theEtape = theEtape;
    }

    public String getTheCodeEtape() {
        return theCodeEtape;
    }

    public void setTheCodeEtape(String theCodeEtape) {
        this.theCodeEtape = theCodeEtape;
    }

    public String getTheCodeVersionEtape() {
        return theCodeVersionEtape;
    }

    public void setTheCodeVersionEtape(String theCodeVersionEtape) {
        this.theCodeVersionEtape = theCodeVersionEtape;
    }

    public Map<String, ElementPedagogique> getElementPedagogiques() {
        return elementPedagogiques;
    }

    public void setElementPedagogiques(Map<String, ElementPedagogique> elementPedagogiques) {
        this.elementPedagogiques = elementPedagogiques;
    }

    public List<ElementPedagogique> getListeELPs() {
        return listeELPs;
    }

    public void setListeELPs(List<ElementPedagogique> listeELPs) {
        this.listeELPs = listeELPs;
    }

    public String getTheCodeElp() {
        return theCodeElp;
    }

    public void setTheCodeElp(String theCodeElp) {
        this.theCodeElp = theCodeElp;
    }

    public String getTheLibElp() {
        return theLibElp;
    }

    public void setTheLibElp(String theLibElp) {
        this.theLibElp = theLibElp;
    }

    public BigDecimal getTheCreditECTS() {
        return theCreditECTS;
    }

    public void setTheCreditECTS(BigDecimal theCreditECTS) {
        this.theCreditECTS = theCreditECTS;
    }

    public Map<String, String> getSteps() {
        return steps;
    }

    public void setSteps(Map<String, String> steps) {
        this.steps = steps;
    }

    public Map<String, String> getStudys() {
        return studys;
    }

    public void setStudys(Map<String, String> studys) {
        this.studys = studys;
    }

    public List<EtapeInscription> getListeEtapeInscriptions() {
        return listeEtapeInscriptions;
    }

    public void setListeEtapeInscriptions(List<EtapeInscription> listeEtapeInscriptions) {
        this.listeEtapeInscriptions = listeEtapeInscriptions;
    }

    public String getTheAssurance() {
        return theAssurance;
    }

    public void setTheAssurance(String theAssurance) {
        this.theAssurance = theAssurance;
    }

    public String getTheCaisseRegime() {
        return theCaisseRegime;
    }

    public void setTheCaisseRegime(String theCaisseRegime) {
        this.theCaisseRegime = theCaisseRegime;
    }

    public AdministrationApogee getAdministrationApogee() {
        return administrationApogee;
    }

    public void setAdministrationApogee(AdministrationApogee administrationApogee) {
        this.administrationApogee = administrationApogee;
    }

    public List<String> getListeAnneesUniv() {
        return listeAnneesUniv;
    }

    public void setListeAnneesUniv(List<String> listeAnneesUniv) {
        this.listeAnneesUniv = listeAnneesUniv;
    }

    public String getVolumeHoraireFormation() {
        return volumeHoraireFormation;
    }

    public void setVolumeHoraireFormation(String volumeHoraireFormation) {
        this.volumeHoraireFormation = volumeHoraireFormation;
    }

    public List<String> getAnneesInscriptionFC() {
        return anneesInscriptionFC;
    }

    public void setAnneesInscriptionFC(List<String> anneesInscriptionFC) {
        this.anneesInscriptionFC = anneesInscriptionFC;
    }

    public List<String> getStepsKey() {
        return stepsKey;
    }

    public void setStepsKey(List<String> stepsKey) {
        this.stepsKey = stepsKey;
    }

    public List<String> getStudysKey() {
        return studysKey;
    }

    public void setStudysKey(List<String> studysKey) {
        this.studysKey = studysKey;
    }
}
