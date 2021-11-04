package org.esup_portail.esup_stage.model;

import org.esup_portail.esup_stage.dto.TemplateMailInterface;

import javax.persistence.*;

@Entity
@Table(name = "TemplateMail")
public class TemplateMail extends ObjetMetier implements TemplateMailInterface {

    @Id
    @Column(nullable = false)
    private int id;

    @Column(nullable = false, length = 150, unique = true)
    private String code;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(nullable = false, length = 250)
    private String objet;

    @Lob
    @Column(nullable = false)
    private String texte;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }
}
