package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import org.esup_portail.esup_stage.dto.view.Views;

@Entity
@Table(name = "FicheEvaluation")
@Data
public class FicheEvaluation implements Exportable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFicheEvaluation", nullable = false)
    private int id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    private CentreGestion centreGestion;

    @JsonView(Views.List.class)
    private Boolean validationEtudiant;
    @JsonView(Views.List.class)
    private Boolean validationEnseignant;
    @JsonView(Views.List.class)
    private Boolean validationEntreprise;

    private Boolean questionEnt1;
    private Boolean questionEnt2;
    private Boolean questionEnt3;
    private Boolean questionEnt4;
    private Boolean questionEnt5;
    private Boolean questionEnt6;
    private Boolean questionEnt7;
    private Boolean questionEnt8;
    private Boolean questionEnt9;
    private Boolean questionEnt10;
    private Boolean questionEnt11;
    private Boolean questionEnt12;
    private Boolean questionEnt13;
    private Boolean questionEnt14;
    private Boolean questionEnt15;
    private Boolean questionEnt16;
    private Boolean questionEnt17;
    private Boolean questionEnt18;
    private Boolean questionEnt19;

    private Boolean questionEtuI1;
    private Boolean questionEtuI2;
    private Boolean questionEtuI3;
    private Boolean questionEtuI4;
    private Boolean questionEtuI5;
    private Boolean questionEtuI6;
    private Boolean questionEtuI7;
    private Boolean questionEtuI8;

    private Boolean questionEtuII1;
    private Boolean questionEtuII2;
    private Boolean questionEtuII3;
    private Boolean questionEtuII4;
    private Boolean questionEtuII5;
    private Boolean questionEtuII6;

    private Boolean questionEtuIII1;
    private Boolean questionEtuIII2;
    private Boolean questionEtuIII3;
    private Boolean questionEtuIII4;
    private Boolean questionEtuIII5;
    private Boolean questionEtuIII6;
    private Boolean questionEtuIII7;
    private Boolean questionEtuIII8;
    private Boolean questionEtuIII9;
    private Boolean questionEtuIII10;
    private Boolean questionEtuIII11;
    private Boolean questionEtuIII12;
    private Boolean questionEtuIII13;
    private Boolean questionEtuIII14;
    private Boolean questionEtuIII15;
    private Boolean questionEtuIII16;

    private Boolean questionEnsI1;
    private Boolean questionEnsI2;
    private Boolean questionEnsI3;

    private Boolean questionEnsII1;
    private Boolean questionEnsII2;
    private Boolean questionEnsII3;
    private Boolean questionEnsII4;
    private Boolean questionEnsII5;
    private Boolean questionEnsII6;
    private Boolean questionEnsII7;
    private Boolean questionEnsII8;
    private Boolean questionEnsII9;
    private Boolean questionEnsII10;
    private Boolean questionEnsII11;

    @Override
    public String getExportValue(String key) {
        return null;
    }
}
