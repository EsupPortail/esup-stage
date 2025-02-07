package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.esup_portail.esup_stage.dto.view.Views;

@Entity
@Table(name = "Etape")
public class Etape implements Exportable {

    @JsonView(Views.List.class)
    @EmbeddedId
    private EtapeId id;

    @JsonView(Views.List.class)
    @Column(name = "libelleEtape", nullable = false)
    private String libelle;

    public EtapeId getId() {
        return id;
    }

    public void setId(EtapeId id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String getExportValue(String key) {
        return null;
    }
}
