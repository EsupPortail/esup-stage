package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "AdminStructure")
public class AdminStructure extends ObjetMetier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAdminStructure", nullable = false)
    private int id;

    @Column
    private String nom;

    @Column
    private String prenom;

    @Column
    private String mail;

    @ManyToOne
    @JoinColumn(name = "idCivilite")
    private Civilite civilite;

    @Temporal(TemporalType.TIMESTAMP)
    private Date derniereConnexion;

    @Temporal(TemporalType.TIMESTAMP)
    private Date avantDerniereConnexion;

    @Column
    private String login;

    @Column
    private String eppn;

    @Column
    private String mdp;

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

    public Civilite getCivilite() {
        return civilite;
    }

    public void setCivilite(Civilite civilite) {
        this.civilite = civilite;
    }

    public Date getDerniereConnexion() {
        return derniereConnexion;
    }

    public void setDerniereConnexion(Date derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }

    public Date getAvantDerniereConnexion() {
        return avantDerniereConnexion;
    }

    public void setAvantDerniereConnexion(Date avantDerniereConnexion) {
        this.avantDerniereConnexion = avantDerniereConnexion;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEppn() {
        return eppn;
    }

    public void setEppn(String eppn) {
        this.eppn = eppn;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }
}
