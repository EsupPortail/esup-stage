package org.esup_portail.esup_stage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "ParamConvention")
@Data
public class ParamConvention implements Exportable {

    @Id
    @Column(nullable = false)
    private String code;

    @Column
    private String libelle;

    @Column
    private String exemple;


    @Override
    public String getExportValue(String key) {
        String value = "";
        switch (key) {
            case "code":
                value = getCode();
                break;
            case "libelle":
                value = getLibelle();
                break;
            case "exemple":
                value = getExemple();
                break;
            default:
                break;
        }
        return value;
    }
}
