package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "ReponseSupplementaire")
public class ReponseSupplementaire {

    @EmbeddedId
    private ReponseSupplementaireId id;

    @JsonIgnore
    @ManyToOne
    @MapsId("idQuestionSupplementaire")
    @JoinColumn(name="idQuestionSupplementaire", nullable = false)
    private QuestionSupplementaire questionSupplementaire;

    @JsonIgnore
    @ManyToOne
    @MapsId("idConvention")
    @JoinColumn(name="idConvention", nullable = false)
    private Convention convention;

    @Lob
    private String reponseTxt;
    private Integer reponseInt;
    private Boolean reponseBool;

    public ReponseSupplementaireId getId() {
        return id;
    }

    public void setId(ReponseSupplementaireId id) {
        this.id = id;
    }

    public QuestionSupplementaire getQuestionSupplementaire() {
        return questionSupplementaire;
    }

    public void setQuestionSupplementaire(QuestionSupplementaire questionSupplementaire) {
        this.questionSupplementaire = questionSupplementaire;
    }

    public Convention getConvention() {
        return convention;
    }

    public void setConvention(Convention convention) {
        this.convention = convention;
    }

    public String getReponseTxt() {
        return reponseTxt;
    }

    public void setReponseTxt(String reponseTxt) {
        this.reponseTxt = reponseTxt;
    }

    public Integer getReponseInt() {
        return reponseInt;
    }

    public void setReponseInt(Integer reponseInt) {
        this.reponseInt = reponseInt;
    }

    public Boolean getReponseBool() {
        return reponseBool;
    }

    public void setReponseBool(Boolean reponseBool) {
        this.reponseBool = reponseBool;
    }
}
