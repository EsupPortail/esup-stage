package org.esup_portail.esup_stage.model;

import org.esup_portail.esup_stage.dto.TemplateMailInterface;

import javax.persistence.*;

@Entity
@Table(name = "TemplateMail")
public class TemplateMail extends ObjetMetier implements TemplateMailInterface {

    public static final String CODE_AVENANT_VALIDATION = "AVENANT_VALIDATION";
    public static final String CODE_CONVENTION_VALID_ADMINISTRATIVE = "CONVENTION_VALID_ADMINISTRATIVE";
    public static final String CODE_CONVENTION_VALID_PEDAGOGIQUE = "CONVENTION_VALID_PEDAGOGIQUE";
    public static final String CODE_ETU_CREA_AVENANT = "ETU_CREA_AVENANT";
    public static final String CODE_ETU_CREA_CONVENTION = "ETU_CREA_CONVENTION";
    public static final String CODE_ETU_MODIF_AVENANT = "ETU_MODIF_AVENANT";
    public static final String CODE_ETU_MODIF_CONVENTION = "ETU_MODIF_CONVENTION";
    public static final String CODE_GES_CREA_AVENANT = "GES_CREA_AVENANT";
    public static final String CODE_GES_CREA_CONVENTION = "GES_CREA_CONVENTION";
    public static final String CODE_GES_MODIF_AVENANT = "GES_MODIF_AVENANT";
    public static final String CODE_GES_MODIF_CONVENTION = "GES_MODIF_CONVENTION";

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
