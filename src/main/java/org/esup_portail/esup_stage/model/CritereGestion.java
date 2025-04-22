package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "CritereGestion")
public class CritereGestion implements Exportable {

    @EmbeddedId
    private CritereGestionId id;

    @Column(name = "libelleCritere", nullable = false)
    private String libelle;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    private CentreGestion centreGestion;

    @Override
    public String getExportValue(String key) {
        return null;
    }
}
