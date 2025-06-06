package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "HistoriqueMailGroupe")
public class HistoriqueMailGroupe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHistoriqueMailGroupe", nullable = false)
    private int id;

    @Column(name = "loginEnvoi", nullable = false)
    private String login;

    @Column(name = "dateEnvoi", nullable = false)
    private Date date;

    @Column(name = "mailto", nullable = false)
    private String mailto;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "idGroupeEtudiant", nullable = false)
    private GroupeEtudiant groupeEtudiant;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMailto() {
        return mailto;
    }

    public void setMailto(String mailto) {
        this.mailto = mailto;
    }

    public GroupeEtudiant getGroupeEtudiant() {
        return groupeEtudiant;
    }

    public void setGroupeEtudiant(GroupeEtudiant groupeEtudiant) {
        this.groupeEtudiant = groupeEtudiant;
    }
}
