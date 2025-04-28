package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import org.esup_portail.esup_stage.dto.view.Views;

@Entity
@Table(name = "StatutJuridique")
@Data
public class StatutJuridique implements Exportable {

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idStatutJuridique", nullable = false)
    private int id;

    @JsonView(Views.List.class)
    @Column(name = "libelleStatutJuridique", nullable = false)
    private String libelle;

    @Column(name = "temEnServStatut", nullable = false, length = 1)
    private String temEnServ;

    @ManyToOne
    @JoinColumn(name = "idTypeStructure", nullable = false)
    private TypeStructure typeStructure;

    private Boolean modifiable;

    @Column
    private String code;

    @Override
    public String getExportValue(String key) {
        String value = "";
        switch (key) {
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
