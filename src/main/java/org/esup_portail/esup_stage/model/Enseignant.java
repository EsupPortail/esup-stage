package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "Enseignant",
        uniqueConstraints = {
                @UniqueConstraint(name = "index_uidEnseignant_codeUniversite", columnNames = {"uidEnseignant", "codeUniversite"})
        }
)
public class Enseignant extends ObjetMetier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEnseignant", nullable = false)
    private int id;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Column(length = 50)
    private String mail;

    @Column(name = "telephone", length = 30)
    private String tel;

    @Column(length = 50)
    private String fax;

    @Column(nullable = false, length = 10)
    private String codeUniversite;

    @Column(length = 50)
    private String typePersonne;

    @Column(nullable = false, length = 50)
    private String uidEnseignant;

    @Column(length = 250)
    private String campus;

    @Column(length = 20)
    private String bureau;

    @Column(length = 45)
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
}
