package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "TemplateConvention",
        uniqueConstraints = {
                @UniqueConstraint(name = "index_idTypeConvention_codeLangueConvention", columnNames = {"idTypeConvention", "codeLangueConvention"})
        }
)
@Data
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
