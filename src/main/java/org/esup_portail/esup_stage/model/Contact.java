package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.view.Views;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Contact")
public class Contact extends ObjetMetier implements Exportable {

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idContact", nullable = false)
    private int id;

    @JsonView(Views.List.class)
    @Column(nullable = false)
    private String nom;

    @JsonView(Views.List.class)
    @Column(nullable = false)
    private String prenom;

    @Column
    private String mail;

    @Column
    private String tel;

    @Column
    private String fax;

    @ManyToOne
    @JoinColumn(name = "idCivilite")
    private Civilite civilite;

    @Temporal(TemporalType.TIMESTAMP)
    private Date derniereConnexion;

    @Temporal(TemporalType.TIMESTAMP)
    private Date avantDerniereConnexion;

    @ManyToOne
    @JoinColumn(name = "idService", nullable = false)
    private Service service;

    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    private CentreGestion centreGestion;

    @Column(nullable = false)
    private String fonction;

    @Column
    private String login;

    @Column
    private String mdp;

    @Lob
    private String commentaire;

    @Temporal(TemporalType.DATE)
    private Date infosAJour;

    @Column
    private String loginInfosAJour;

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

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
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

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public CentreGestion getCentreGestion() {
        return centreGestion;
    }

    public void setCentreGestion(CentreGestion centreGestion) {
        this.centreGestion = centreGestion;
    }

    public String getFonction() {
        return fonction;
    }

    public void setFonction(String fonction) {
        this.fonction = fonction;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
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

    @Override
    public String getExportValue(String key) {
        return null;
    }
}
