package org.esup_portail.esup_stage.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "Utilisateur")
public class Utilisateur implements Exportable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUtilisateur", nullable = false)
    private int id;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(unique = true)
    private String uid;

    @Column
    private String password;

    @Column
    private String nom;

    @Column
    private String prenom;

    @Column(nullable = false)
    private Boolean actif;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date dateCreation;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "UtilisateurRole",
            joinColumns = @JoinColumn(name = "idUtilisateur"),
            inverseJoinColumns = @JoinColumn(name = "idRole")
    )
    private List<Role> roles = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Boolean isActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    @PrePersist
    public void prePersist() {
        setDateCreation(new Date());
    }

    @Override
    public String getExportValue(String key) {
        String value = "";
        switch (key) {
            case "login":
                value = getLogin();
                break;
            case "nom":
                value = getNom();
                break;
            case "prenom":
                value = getPrenom();
                break;
            case "roles":
                if (getRoles() != null) {
                    value = getRoles().stream().map(Role::getLibelle).collect(Collectors.joining(", "));
                }
                break;
            case "actif":
                value = isActif() != null && isActif() ? "Oui" : "Non";
                break;
            default:
                break;
        }
        return value;
    }
}
