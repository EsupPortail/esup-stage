package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "Contenu")
public class Contenu extends ObjetMetier implements Exportable {

    @Id
    @Column(name = "codeContenu", nullable = false)
    private String code;

    @Column(nullable = false)
    private boolean libelle = true;

    @Lob
    @Column(nullable = false)
    private String texte;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isLibelle() {
        return libelle;
    }

    public void setLibelle(boolean libelle) {
        this.libelle = libelle;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    @Override
    public String getExportValue(String key) {
        String value = "";
        switch (key) {
            case "code":
                value = getCode();
                break;
            case "texte":
                value = getTexte();
                break;
            default:
                break;
        }
        return value;
    }
}
