package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import org.esup_portail.esup_stage.dto.view.Views;

@Entity
@Table(name = "ReponseEvaluation")
@Data
public class ReponseEvaluation {

    @EmbeddedId
    private ReponseEvaluationId reponseEvaluationId;

    @JsonIgnore
    @ManyToOne
    @MapsId("idFicheEvaluation")
    @JoinColumn(name = "idFicheEvaluation")
    private FicheEvaluation ficheEvaluation;

    @JsonIgnore
    @OneToOne
    @MapsId("idConvention")
    @JoinColumn(name = "idConvention")
    private Convention convention;

    @JsonView(Views.List.class)
    private Boolean validationEtudiant;
    @JsonView(Views.List.class)
    private Boolean validationEnseignant;
    @JsonView(Views.List.class)
    private Boolean validationEntreprise;
    @JsonView(Views.List.class)
    private Boolean impressionEtudiant;
    @JsonView(Views.List.class)
    private Boolean impressionEnseignant;
    @JsonView(Views.List.class)
    private Boolean impressionEntreprise;

    private Integer reponseEnt1;
    @Lob
    private String reponseEnt1bis;
    private Integer reponseEnt2;
    @Lob
    private String reponseEnt2bis;
    private Integer reponseEnt3;
    private Integer reponseEnt4;
    @Lob
    private String reponseEnt4bis;
    private Integer reponseEnt5;
    @Lob
    private String reponseEnt5bis;
    private Integer reponseEnt6;
    @Lob
    private String reponseEnt6bis;
    private Integer reponseEnt7;
    @Lob
    private String reponseEnt7bis;
    private Integer reponseEnt8;
    @Lob
    private String reponseEnt8bis;
    private Integer reponseEnt9;
    @Lob
    private String reponseEnt9bis;
    private Boolean reponseEnt10;
    @Lob
    private String reponseEnt10bis;
    private Integer reponseEnt11;
    @Lob
    private String reponseEnt11bis;
    private Integer reponseEnt12;
    @Lob
    private String reponseEnt12bis;
    private Integer reponseEnt13;
    @Lob
    private String reponseEnt13bis;
    private Integer reponseEnt14;
    @Lob
    private String reponseEnt14bis;
    private Integer reponseEnt15;
    @Lob
    private String reponseEnt15bis;
    private Integer reponseEnt16;
    @Lob
    private String reponseEnt16bis;
    private Integer reponseEnt17;
    @Lob
    private String reponseEnt17bis;
    private Boolean reponseEnt18;
    @Lob
    private String reponseEnt18bis;
    @Lob
    private String reponseEnt19;

    private Integer reponseEtuI1;
    @Lob
    private String reponseEtuI1bis;
    private Integer reponseEtuI2;
    private Integer reponseEtuI3;
    private Boolean reponseEtuI4a;
    private Boolean reponseEtuI4b;
    private Boolean reponseEtuI4c;
    private Boolean reponseEtuI4d;
    private Integer reponseEtuI5;
    private Integer reponseEtuI6;
    private Boolean reponseEtuI7;
    private Integer reponseEtuI7bis1;
    private Integer reponseEtuI7bis1a;
    private String reponseEtuI7bis1b;
    private Integer reponseEtuI7bis2;
    private Boolean reponseEtuI8;

    private Integer reponseEtuII1;
    @Lob
    private String reponseEtuII1bis;
    private Integer reponseEtuII2;
    @Lob
    private String reponseEtuII2bis;
    private Integer reponseEtuII3;
    @Lob
    private String reponseEtuII3bis;
    private Integer reponseEtuII4;
    private Boolean reponseEtuII5;
    private Integer reponseEtuII5a;
    private Boolean reponseEtuII5b;
    private Boolean reponseEtuII6;

    private Boolean reponseEtuIII1;
    @Lob
    private String reponseEtuIII1bis;
    private Boolean reponseEtuIII2;
    @Lob
    private String reponseEtuIII2bis;
    private Boolean reponseEtuIII3;
    @Lob
    private String reponseEtuIII3bis;
    private Integer reponseEtuIII4;
    private Boolean reponseEtuIII5a;
    private Boolean reponseEtuIII5b;
    private Boolean reponseEtuIII5c;
    @Lob
    private String reponseEtuIII5bis;
    private Integer reponseEtuIII6;
    @Lob
    private String reponseEtuIII6bis;
    private Integer reponseEtuIII7;
    @Lob
    private String reponseEtuIII7bis;
    private Boolean reponseEtuIII8;
    @Lob
    private String reponseEtuIII8bis;
    private Boolean reponseEtuIII9;
    @Lob
    private String reponseEtuIII9bis;
    private Boolean reponseEtuIII10;
    private Boolean reponseEtuIII11;
    private Boolean reponseEtuIII12;
    private Boolean reponseEtuIII13;
    private Boolean reponseEtuIII14;
    private Integer reponseEtuIII15;
    @Lob
    private String reponseEtuIII15bis;
    private Integer reponseEtuIII16;
    @Lob
    private String reponseEtuIII16bis;

    private Boolean reponseEnsI1a;
    private Boolean reponseEnsI1b;
    private Boolean reponseEnsI1c;
    private Boolean reponseEnsI2a;
    private Boolean reponseEnsI2b;
    private Boolean reponseEnsI2c;
    @Lob
    private String reponseEnsI3;

    private Integer reponseEnsII1;
    private Integer reponseEnsII2;
    private Integer reponseEnsII3;
    private Integer reponseEnsII4;
    private Integer reponseEnsII5;
    private Integer reponseEnsII6;
    private Integer reponseEnsII7;
    private Integer reponseEnsII8;
    private Integer reponseEnsII9;
    private Integer reponseEnsII10;
    @Lob
    private String reponseEnsII11;

}
