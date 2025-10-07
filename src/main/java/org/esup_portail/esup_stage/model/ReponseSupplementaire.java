package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ReponseSupplementaire")
@Data
public class ReponseSupplementaire {

    @EmbeddedId
    private ReponseSupplementaireId id;

    @JsonIgnore
    @ManyToOne
    @MapsId("idQuestionSupplementaire")
    @JoinColumn(name = "idQuestionSupplementaire", nullable = false)
    private QuestionSupplementaire questionSupplementaire;

    @JsonIgnore
    @ManyToOne
    @MapsId("idConvention")
    @JoinColumn(name = "idConvention", nullable = false)
    private Convention convention;

    @Lob
    private String reponseTxt;
    private Integer reponseInt;
    private Boolean reponseBool;

}
