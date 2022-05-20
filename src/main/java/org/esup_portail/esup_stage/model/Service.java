package org.esup_portail.esup_stage.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Service")
public class Service extends ObjetMetier implements Exportable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idService", nullable = false)
    private int id;

    @Column(nullable = false)
    private String nom;

    @Temporal(TemporalType.DATE)
    private Date infosAJour;

    @Column
    private String loginInfosAJour;

    @ManyToOne
    @JoinColumn(name = "idStructure", nullable = false)
    private Structure structure;

    @Column
    private String telephone;

    @Column
    private String batimentResidence;

    @Column(nullable = false)
    private String voie;

    @Column
    private String commune;

    @Column(nullable = false)
    private String codePostal;

    @Column
    private String codeCommune;

    @ManyToOne
    @JoinColumn(name = "idPays", nullable = false)
    private Pays pays;

    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    private CentreGestion centreGestion;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
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

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
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

    public CentreGestion getCentreGestion() {
        return centreGestion;
    }

    public void setCentreGestion(CentreGestion centreGestion) {
        this.centreGestion = centreGestion;
    }

    @Override
    public String getExportValue(String key) {
        return null;
    }
}
