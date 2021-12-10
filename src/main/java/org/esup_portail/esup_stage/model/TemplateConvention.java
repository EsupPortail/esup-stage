package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "TemplateConvention",
        uniqueConstraints = {
                @UniqueConstraint(name = "index_idTypeConvention_codeLangueConvention", columnNames = {"idTypeConvention", "codeLangueConvention"})
        }
)
public class TemplateConvention extends ObjetMetier implements Exportable {

    @Id
    @Column(nullable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "idTypeConvention", nullable = false)
    private TypeConvention typeConvention;

    @ManyToOne
    @JoinColumn(name = "codeLangueConvention", nullable = false)
    private LangueConvention langueConvention;

    @Lob
    @Column
    private String texte;

    @Lob
    @Column
    private String texteAvenant;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TypeConvention getTypeConvention() {
        return typeConvention;
    }

    public void setTypeConvention(TypeConvention typeConvention) {
        this.typeConvention = typeConvention;
    }

    public LangueConvention getLangueConvention() {
        return langueConvention;
    }

    public void setLangueConvention(LangueConvention langueConvention) {
        this.langueConvention = langueConvention;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public String getTexteAvenant() {
        return texteAvenant;
    }

    public void setTexteAvenant(String texteAvenant) {
        this.texteAvenant = texteAvenant;
    }

    @Override
    public String getExportValue(String key) {
        String value = "";
        switch (key) {
            case "typeConvention":
                value = getTypeConvention().getLibelle();
                break;
            case "langueConvention":
                value = getLangueConvention().getLibelle();
                break;
            default:
                break;
        }
        return value;
    }
}
