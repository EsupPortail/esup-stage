package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "CronTask")
@Data
public class CronTask implements Exportable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String nom;

    @Column
    private String expressionCron;

    @Column
    private Date dateCreation;

    @Column
    private String loginCreation;

    @Column
    private Date dateModification;

    @Column
    private String loginModification;

    @Column
    private Date dateDernierExecution;

    @Column
    private boolean active;


    @Override
    public String getExportValue(String key) {
        return switch (key) {
            case "id" -> id != null ? id.toString() : "";
            case "nom" -> nom != null ? nom : "";
            case "expressionCron" -> expressionCron != null ? expressionCron : "";
            case "dateCreation" -> dateCreation != null ? dateCreation.toString() : "";
            case "loginCreation" -> loginCreation != null ? loginCreation : "";
            case "dateModification" -> dateModification != null ? dateModification.toString() : "";
            case "loginModification" -> loginModification != null ? loginModification : "";
            case "dateDernierExecution" -> dateDernierExecution != null ? dateDernierExecution.toString() : "";
            case "active" -> Boolean.toString(active);
            default -> "";
        };
    }
}
