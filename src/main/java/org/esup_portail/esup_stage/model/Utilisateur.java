package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
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
    private Boolean actif=false;

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

    @Column(unique = true)
    private String numEtudiant;

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
                value = getActif() != null && getActif() ? "Oui" : "Non";
                break;
            default:
                break;
        }
        return value;
    }
}
