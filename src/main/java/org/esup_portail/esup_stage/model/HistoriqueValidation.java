package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "HistoriqueValidation")
public class HistoriqueValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHistoriqueValidation", nullable = false)
    private int id;

    @Column(name = "loginModification", nullable = false)
    private String login;

    @Column(name = "typeValidation", nullable = false)
    private String type;

    private Boolean valeurAvant;

    private Boolean valeurApres;

    @Column(name = "dateValidation", nullable = false)
    private Date date;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "idConvention", nullable = false)
    private Convention convention;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getValeurAvant() {
        return valeurAvant;
    }

    public void setValeurAvant(Boolean valeurAvant) {
        this.valeurAvant = valeurAvant;
    }

    public Boolean getValeurApres() {
        return valeurApres;
    }

    public void setValeurApres(Boolean valeurApres) {
        this.valeurApres = valeurApres;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Convention getConvention() {
        return convention;
    }

    public void setConvention(Convention convention) {
        this.convention = convention;
    }

    @PrePersist
    public void prePersist() {
        this.setDate(new Date());
    }
}
