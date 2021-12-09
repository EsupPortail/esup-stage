package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.view.Views;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "PeriodeInterruptionAvenant")
public class PeriodeInterruptionAvenant {

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @JsonView(Views.List.class)
    @Temporal(TemporalType.DATE)
    private Date dateDebutInterruption;

    @JsonView(Views.List.class)
    @Temporal(TemporalType.DATE)
    private Date dateFinInterruption;

    @JsonView(Views.List.class)
    @Column(nullable = false)
    private Boolean isModif;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumn(name = "idPeriodeInterruptionStage")
    private PeriodeInterruptionStage periodeInterruptionStage;

    @ManyToOne
    @JoinColumn(name = "idAvenant", nullable = false)
    private Avenant avenant;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDateDebutInterruption() {
        return dateDebutInterruption;
    }

    public void setDateDebutInterruption(Date dateDebutInterruption) {
        this.dateDebutInterruption = dateDebutInterruption;
    }

    public Date getDateFinInterruption() {
        return dateFinInterruption;
    }

    public void setDateFinInterruption(Date dateFinInterruption) {
        this.dateFinInterruption = dateFinInterruption;
    }

    public Boolean getIsModif() {
        return isModif;
    }

    public void setIsModif(Boolean modif) {
        isModif = modif;
    }

    public PeriodeInterruptionStage getPeriodeInterruptionStage() {
        return periodeInterruptionStage;
    }

    public void setPeriodeInterruptionStage(PeriodeInterruptionStage periodeInterruptionStage) {
        this.periodeInterruptionStage = periodeInterruptionStage;
    }

    public Avenant getAvenant() {
        return avenant;
    }

    public void setAvenant(Avenant avenant) {
        this.avenant = avenant;
    }
}
