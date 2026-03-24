package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
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

    // Legacy relation kept during transition to avoid breaking existing queries/controllers
    @ManyToOne
    @JoinColumn(name = "idTypeConvention", nullable = false)
    private TypeConvention typeConvention;

    @ManyToMany
    @JoinTable(
            name = "TypeTemplateConvention",
            joinColumns = @JoinColumn(name = "idTemplateConvention"),
            inverseJoinColumns = @JoinColumn(name = "idTypeConvention")
    )
    private List<TypeConvention> typeConventions = new ArrayList<>();

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
                if (getTypeConvention() != null) {
                    value = getTypeConvention().getLibelle();
                } else {
                    value = getTypeConventions().stream().map(TypeConvention::getLibelle).reduce((a, b) -> a + ", " + b).orElse("");
                }
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
