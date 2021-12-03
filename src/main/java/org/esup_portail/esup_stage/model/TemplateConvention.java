package org.esup_portail.esup_stage.model;

import javax.persistence.*;

@Entity
@Table(name = "TemplateConvention",
        uniqueConstraints = {
                @UniqueConstraint(name = "index_idTypeConvention_codeLangueConvention", columnNames = {"idTypeConvention", "codeLangueConvention"})
        }
)
public class TemplateConvention extends ObjetMetier {

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
}
