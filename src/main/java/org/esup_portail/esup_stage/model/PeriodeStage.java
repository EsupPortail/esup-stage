package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import org.esup_portail.esup_stage.dto.view.Views;

import java.util.Date;

@Entity
@Table(name = "PeriodeStage")
@Data
public class PeriodeStage implements Exportable {

    @Id
    @JsonView(Views.List.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @JsonView(Views.List.class)
    @Temporal(TemporalType.DATE)
    @Column
    private Date dateDebut;

    @JsonView(Views.List.class)
    @Temporal(TemporalType.DATE)
    @Column
    private Date dateFin;

    @JsonView(Views.List.class)
    @Column
    private Integer nbHeuresJournalieres;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "convention_id")
    private Convention convention;

    @Override
    public String getExportValue(String key) {
        return null;
    }
}