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

}
