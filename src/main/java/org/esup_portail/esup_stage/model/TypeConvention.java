package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "TypeConvention")
public class TypeConvention implements Exportable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTypeConvention", nullable = false)
    private int id;

    @Column(name = "libelleTypeConvention", nullable = false)
    private String libelle;

    @Column(nullable = false)
    private String codeCtrl;

    @Column(name = "temEnServTypeConvention", nullable = false, length = 1)
    private String temEnServ;

    private Boolean modifiable;

    @JsonIgnore
    @OneToMany(mappedBy = "typeConvention")
    private List<TemplateConvention> templates = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "typeConventions")
    private List<TemplateConvention> templateConventions = new ArrayList<>();

    @Override
    public String getExportValue(String key) {
        String value = "";
        switch (key) {
            case "codeCtrl":
                value = getCodeCtrl();
                break;
            case "libelle":
                value = getLibelle();
                break;
            case "actif":
                value = getTemEnServ().equals("O") ? "Oui" : "Non";
                break;
            default:
                break;
        }
        return value;
    }
}
