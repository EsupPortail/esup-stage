package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.view.Views;

import javax.persistence.*;

@Entity
@Table(name = "Enseignant",
        uniqueConstraints = {
                @UniqueConstraint(name = "index_uidEnseignant_codeUniversite", columnNames = {"uidEnseignant", "codeUniversite"})
        }
)
public class Enseignant extends ObjetMetier implements Exportable {

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEnseignant", nullable = false)
    private int id;

    @JsonView(Views.List.class)
    @Column(nullable = false)
    private String nom;

    @JsonView(Views.List.class)
    @Column(nullable = false)
    private String prenom;

    @Column
    private String mail;

    @Column(name = "telephone")
    private String tel;

    @Column
    private String fax;

    @Column(nullable = false)
    private String codeUniversite;

    @Column
    private String typePersonne;

    @Column(nullable = false)
    private String uidEnseignant;

    @Column
    private String campus;

    @Column
    private String bureau;

    @Column
    private String batiment;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "codeAffectation", referencedColumnName = "codeAffectation", nullable = false),
            @JoinColumn(name = "codeUniversiteAffectation", referencedColumnName = "codeUniversite", nullable = false),
    })
    private Affectation affectation;

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

    public String getCodeUniversite() {
        return codeUniversite;
    }

    public void setCodeUniversite(String codeUniversite) {
        this.codeUniversite = codeUniversite;
    }

    public String getTypePersonne() {
        return typePersonne;
    }

    public void setTypePersonne(String typePersonne) {
        this.typePersonne = typePersonne;
    }

    public String getUidEnseignant() {
        return uidEnseignant;
    }

    public void setUidEnseignant(String uidEnseignant) {
        this.uidEnseignant = uidEnseignant;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getBureau() {
        return bureau;
    }

    public void setBureau(String bureau) {
        this.bureau = bureau;
    }

    public String getBatiment() {
        return batiment;
    }

    public void setBatiment(String batiment) {
        this.batiment = batiment;
    }

    public Affectation getAffectation() {
        return affectation;
    }

    public void setAffectation(Affectation affectation) {
        this.affectation = affectation;
    }

    @Override
    public String getExportValue(String key) {
        return null;
    }
}
