package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.view.Views;

import javax.persistence.*;

@Entity
@Table(name = "ReponseEvaluation")
public class ReponseEvaluation {

    @EmbeddedId
    private ReponseEvaluationId reponseEvaluationId;

    @JsonIgnore
    @ManyToOne
    @MapsId("idFicheEvaluation")
    @JoinColumn(name="idFicheEvaluation")
    private FicheEvaluation ficheEvaluation;

    @JsonIgnore
    @OneToOne
    @MapsId("idConvention")
    @JoinColumn(name="idConvention")
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
    private Integer reponseEtuI7bis1b;
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

    public ReponseEvaluationId getReponseEvaluationId() {
        return reponseEvaluationId;
    }

    public void setReponseEvaluationId(ReponseEvaluationId reponseEvaluationId) {
        this.reponseEvaluationId = reponseEvaluationId;
    }

    public FicheEvaluation getFicheEvaluation() {
        return ficheEvaluation;
    }

    public void setFicheEvaluation(FicheEvaluation ficheEvaluation) {
        this.ficheEvaluation = ficheEvaluation;
    }

    public Convention getConvention() {
        return convention;
    }

    public void setConvention(Convention convention) {
        this.convention = convention;
    }

    public Boolean getValidationEtudiant() {
        return validationEtudiant;
    }

    public void setValidationEtudiant(Boolean validationEtudiant) {
        this.validationEtudiant = validationEtudiant;
    }

    public Boolean getValidationEnseignant() {
        return validationEnseignant;
    }

    public void setValidationEnseignant(Boolean validationEnseignant) {
        this.validationEnseignant = validationEnseignant;
    }

    public Boolean getValidationEntreprise() {
        return validationEntreprise;
    }

    public void setValidationEntreprise(Boolean validationEntreprise) {
        this.validationEntreprise = validationEntreprise;
    }

    public Boolean getImpressionEtudiant() {
        return impressionEtudiant;
    }

    public void setImpressionEtudiant(Boolean impressionEtudiant) {
        this.impressionEtudiant = impressionEtudiant;
    }

    public Boolean getImpressionEnseignant() {
        return impressionEnseignant;
    }

    public void setImpressionEnseignant(Boolean impressionEnseignant) {
        this.impressionEnseignant = impressionEnseignant;
    }

    public Boolean getImpressionEntreprise() {
        return impressionEntreprise;
    }

    public void setImpressionEntreprise(Boolean impressionEntreprise) {
        this.impressionEntreprise = impressionEntreprise;
    }

    public Integer getReponseEnt1() {
        return reponseEnt1;
    }

    public void setReponseEnt1(Integer reponseEnt1) {
        this.reponseEnt1 = reponseEnt1;
    }

    public String getReponseEnt1bis() {
        return reponseEnt1bis;
    }

    public void setReponseEnt1bis(String reponseEnt1bis) {
        this.reponseEnt1bis = reponseEnt1bis;
    }

    public Integer getReponseEnt2() {
        return reponseEnt2;
    }

    public void setReponseEnt2(Integer reponseEnt2) {
        this.reponseEnt2 = reponseEnt2;
    }

    public String getReponseEnt2bis() {
        return reponseEnt2bis;
    }

    public void setReponseEnt2bis(String reponseEnt2bis) {
        this.reponseEnt2bis = reponseEnt2bis;
    }

    public Integer getReponseEnt3() {
        return reponseEnt3;
    }

    public void setReponseEnt3(Integer reponseEnt3) {
        this.reponseEnt3 = reponseEnt3;
    }

    public Integer getReponseEnt4() {
        return reponseEnt4;
    }

    public void setReponseEnt4(Integer reponseEnt4) {
        this.reponseEnt4 = reponseEnt4;
    }

    public String getReponseEnt4bis() {
        return reponseEnt4bis;
    }

    public void setReponseEnt4bis(String reponseEnt4bis) {
        this.reponseEnt4bis = reponseEnt4bis;
    }

    public Integer getReponseEnt5() {
        return reponseEnt5;
    }

    public void setReponseEnt5(Integer reponseEnt5) {
        this.reponseEnt5 = reponseEnt5;
    }

    public String getReponseEnt5bis() {
        return reponseEnt5bis;
    }

    public void setReponseEnt5bis(String reponseEnt5bis) {
        this.reponseEnt5bis = reponseEnt5bis;
    }

    public Integer getReponseEnt6() {
        return reponseEnt6;
    }

    public void setReponseEnt6(Integer reponseEnt6) {
        this.reponseEnt6 = reponseEnt6;
    }

    public String getReponseEnt6bis() {
        return reponseEnt6bis;
    }

    public void setReponseEnt6bis(String reponseEnt6bis) {
        this.reponseEnt6bis = reponseEnt6bis;
    }

    public Integer getReponseEnt7() {
        return reponseEnt7;
    }

    public void setReponseEnt7(Integer reponseEnt7) {
        this.reponseEnt7 = reponseEnt7;
    }

    public String getReponseEnt7bis() {
        return reponseEnt7bis;
    }

    public void setReponseEnt7bis(String reponseEnt7bis) {
        this.reponseEnt7bis = reponseEnt7bis;
    }

    public Integer getReponseEnt8() {
        return reponseEnt8;
    }

    public void setReponseEnt8(Integer reponseEnt8) {
        this.reponseEnt8 = reponseEnt8;
    }

    public String getReponseEnt8bis() {
        return reponseEnt8bis;
    }

    public void setReponseEnt8bis(String reponseEnt8bis) {
        this.reponseEnt8bis = reponseEnt8bis;
    }

    public Integer getReponseEnt9() {
        return reponseEnt9;
    }

    public void setReponseEnt9(Integer reponseEnt9) {
        this.reponseEnt9 = reponseEnt9;
    }

    public String getReponseEnt9bis() {
        return reponseEnt9bis;
    }

    public void setReponseEnt9bis(String reponseEnt9bis) {
        this.reponseEnt9bis = reponseEnt9bis;
    }

    public Boolean getReponseEnt10() {
        return reponseEnt10;
    }

    public void setReponseEnt10(Boolean reponseEnt10) {
        this.reponseEnt10 = reponseEnt10;
    }

    public String getReponseEnt10bis() {
        return reponseEnt10bis;
    }

    public void setReponseEnt10bis(String reponseEnt10bis) {
        this.reponseEnt10bis = reponseEnt10bis;
    }

    public Integer getReponseEnt11() {
        return reponseEnt11;
    }

    public void setReponseEnt11(Integer reponseEnt11) {
        this.reponseEnt11 = reponseEnt11;
    }

    public String getReponseEnt11bis() {
        return reponseEnt11bis;
    }

    public void setReponseEnt11bis(String reponseEnt11bis) {
        this.reponseEnt11bis = reponseEnt11bis;
    }

    public Integer getReponseEnt12() {
        return reponseEnt12;
    }

    public void setReponseEnt12(Integer reponseEnt12) {
        this.reponseEnt12 = reponseEnt12;
    }

    public String getReponseEnt12bis() {
        return reponseEnt12bis;
    }

    public void setReponseEnt12bis(String reponseEnt12bis) {
        this.reponseEnt12bis = reponseEnt12bis;
    }

    public Integer getReponseEnt13() {
        return reponseEnt13;
    }

    public void setReponseEnt13(Integer reponseEnt13) {
        this.reponseEnt13 = reponseEnt13;
    }

    public String getReponseEnt13bis() {
        return reponseEnt13bis;
    }

    public void setReponseEnt13bis(String reponseEnt13bis) {
        this.reponseEnt13bis = reponseEnt13bis;
    }

    public Integer getReponseEnt14() {
        return reponseEnt14;
    }

    public void setReponseEnt14(Integer reponseEnt14) {
        this.reponseEnt14 = reponseEnt14;
    }

    public String getReponseEnt14bis() {
        return reponseEnt14bis;
    }

    public void setReponseEnt14bis(String reponseEnt14bis) {
        this.reponseEnt14bis = reponseEnt14bis;
    }

    public Integer getReponseEnt15() {
        return reponseEnt15;
    }

    public void setReponseEnt15(Integer reponseEnt15) {
        this.reponseEnt15 = reponseEnt15;
    }

    public String getReponseEnt15bis() {
        return reponseEnt15bis;
    }

    public void setReponseEnt15bis(String reponseEnt15bis) {
        this.reponseEnt15bis = reponseEnt15bis;
    }

    public Integer getReponseEnt16() {
        return reponseEnt16;
    }

    public void setReponseEnt16(Integer reponseEnt16) {
        this.reponseEnt16 = reponseEnt16;
    }

    public String getReponseEnt16bis() {
        return reponseEnt16bis;
    }

    public void setReponseEnt16bis(String reponseEnt16bis) {
        this.reponseEnt16bis = reponseEnt16bis;
    }

    public Integer getReponseEnt17() {
        return reponseEnt17;
    }

    public void setReponseEnt17(Integer reponseEnt17) {
        this.reponseEnt17 = reponseEnt17;
    }

    public String getReponseEnt17bis() {
        return reponseEnt17bis;
    }

    public void setReponseEnt17bis(String reponseEnt17bis) {
        this.reponseEnt17bis = reponseEnt17bis;
    }

    public Boolean getReponseEnt18() {
        return reponseEnt18;
    }

    public void setReponseEnt18(Boolean reponseEnt18) {
        this.reponseEnt18 = reponseEnt18;
    }

    public String getReponseEnt18bis() {
        return reponseEnt18bis;
    }

    public void setReponseEnt18bis(String reponseEnt18bis) {
        this.reponseEnt18bis = reponseEnt18bis;
    }

    public String getReponseEnt19() {
        return reponseEnt19;
    }

    public void setReponseEnt19(String reponseEnt19) {
        this.reponseEnt19 = reponseEnt19;
    }

    public Integer getReponseEtuI1() {
        return reponseEtuI1;
    }

    public void setReponseEtuI1(Integer reponseEtuI1) {
        this.reponseEtuI1 = reponseEtuI1;
    }

    public String getReponseEtuI1bis() {
        return reponseEtuI1bis;
    }

    public void setReponseEtuI1bis(String reponseEtuI1bis) {
        this.reponseEtuI1bis = reponseEtuI1bis;
    }

    public Integer getReponseEtuI2() {
        return reponseEtuI2;
    }

    public void setReponseEtuI2(Integer reponseEtuI2) {
        this.reponseEtuI2 = reponseEtuI2;
    }

    public Integer getReponseEtuI3() {
        return reponseEtuI3;
    }

    public void setReponseEtuI3(Integer reponseEtuI3) {
        this.reponseEtuI3 = reponseEtuI3;
    }

    public Boolean getReponseEtuI4a() {
        return reponseEtuI4a;
    }

    public void setReponseEtuI4a(Boolean reponseEtuI4a) {
        this.reponseEtuI4a = reponseEtuI4a;
    }

    public Boolean getReponseEtuI4b() {
        return reponseEtuI4b;
    }

    public void setReponseEtuI4b(Boolean reponseEtuI4b) {
        this.reponseEtuI4b = reponseEtuI4b;
    }

    public Boolean getReponseEtuI4c() {
        return reponseEtuI4c;
    }

    public void setReponseEtuI4c(Boolean reponseEtuI4c) {
        this.reponseEtuI4c = reponseEtuI4c;
    }

    public Boolean getReponseEtuI4d() {
        return reponseEtuI4d;
    }

    public void setReponseEtuI4d(Boolean reponseEtuI4d) {
        this.reponseEtuI4d = reponseEtuI4d;
    }

    public Integer getReponseEtuI5() {
        return reponseEtuI5;
    }

    public void setReponseEtuI5(Integer reponseEtuI5) {
        this.reponseEtuI5 = reponseEtuI5;
    }

    public Integer getReponseEtuI6() {
        return reponseEtuI6;
    }

    public void setReponseEtuI6(Integer reponseEtuI6) {
        this.reponseEtuI6 = reponseEtuI6;
    }

    public Boolean getReponseEtuI7() {
        return reponseEtuI7;
    }

    public void setReponseEtuI7(Boolean reponseEtuI7) {
        this.reponseEtuI7 = reponseEtuI7;
    }

    public Integer getReponseEtuI7bis1() {
        return reponseEtuI7bis1;
    }

    public void setReponseEtuI7bis1(Integer reponseEtuI7bis1) {
        this.reponseEtuI7bis1 = reponseEtuI7bis1;
    }

    public Integer getReponseEtuI7bis1a() {
        return reponseEtuI7bis1a;
    }

    public void setReponseEtuI7bis1a(Integer reponseEtuI7bis1a) {
        this.reponseEtuI7bis1a = reponseEtuI7bis1a;
    }

    public Integer getReponseEtuI7bis1b() {
        return reponseEtuI7bis1b;
    }

    public void setReponseEtuI7bis1b(Integer reponseEtuI7bis1b) {
        this.reponseEtuI7bis1b = reponseEtuI7bis1b;
    }

    public Integer getReponseEtuI7bis2() {
        return reponseEtuI7bis2;
    }

    public void setReponseEtuI7bis2(Integer reponseEtuI7bis2) {
        this.reponseEtuI7bis2 = reponseEtuI7bis2;
    }

    public Boolean getReponseEtuI8() {
        return reponseEtuI8;
    }

    public void setReponseEtuI8(Boolean reponseEtuI8) {
        this.reponseEtuI8 = reponseEtuI8;
    }

    public Integer getReponseEtuII1() {
        return reponseEtuII1;
    }

    public void setReponseEtuII1(Integer reponseEtuII1) {
        this.reponseEtuII1 = reponseEtuII1;
    }

    public String getReponseEtuII1bis() {
        return reponseEtuII1bis;
    }

    public void setReponseEtuII1bis(String reponseEtuII1bis) {
        this.reponseEtuII1bis = reponseEtuII1bis;
    }

    public Integer getReponseEtuII2() {
        return reponseEtuII2;
    }

    public void setReponseEtuII2(Integer reponseEtuII2) {
        this.reponseEtuII2 = reponseEtuII2;
    }

    public String getReponseEtuII2bis() {
        return reponseEtuII2bis;
    }

    public void setReponseEtuII2bis(String reponseEtuII2bis) {
        this.reponseEtuII2bis = reponseEtuII2bis;
    }

    public Integer getReponseEtuII3() {
        return reponseEtuII3;
    }

    public void setReponseEtuII3(Integer reponseEtuII3) {
        this.reponseEtuII3 = reponseEtuII3;
    }

    public String getReponseEtuII3bis() {
        return reponseEtuII3bis;
    }

    public void setReponseEtuII3bis(String reponseEtuII3bis) {
        this.reponseEtuII3bis = reponseEtuII3bis;
    }

    public Integer getReponseEtuII4() {
        return reponseEtuII4;
    }

    public void setReponseEtuII4(Integer reponseEtuII4) {
        this.reponseEtuII4 = reponseEtuII4;
    }

    public Boolean getReponseEtuII5() {
        return reponseEtuII5;
    }

    public void setReponseEtuII5(Boolean reponseEtuII5) {
        this.reponseEtuII5 = reponseEtuII5;
    }

    public Integer getReponseEtuII5a() {
        return reponseEtuII5a;
    }

    public void setReponseEtuII5a(Integer reponseEtuII5a) {
        this.reponseEtuII5a = reponseEtuII5a;
    }

    public Boolean getReponseEtuII5b() {
        return reponseEtuII5b;
    }

    public void setReponseEtuII5b(Boolean reponseEtuII5b) {
        this.reponseEtuII5b = reponseEtuII5b;
    }

    public Boolean getReponseEtuII6() {
        return reponseEtuII6;
    }

    public void setReponseEtuII6(Boolean reponseEtuII6) {
        this.reponseEtuII6 = reponseEtuII6;
    }

    public Boolean getReponseEtuIII1() {
        return reponseEtuIII1;
    }

    public void setReponseEtuIII1(Boolean reponseEtuIII1) {
        this.reponseEtuIII1 = reponseEtuIII1;
    }

    public String getReponseEtuIII1bis() {
        return reponseEtuIII1bis;
    }

    public void setReponseEtuIII1bis(String reponseEtuIII1bis) {
        this.reponseEtuIII1bis = reponseEtuIII1bis;
    }

    public Boolean getReponseEtuIII2() {
        return reponseEtuIII2;
    }

    public void setReponseEtuIII2(Boolean reponseEtuIII2) {
        this.reponseEtuIII2 = reponseEtuIII2;
    }

    public String getReponseEtuIII2bis() {
        return reponseEtuIII2bis;
    }

    public void setReponseEtuIII2bis(String reponseEtuIII2bis) {
        this.reponseEtuIII2bis = reponseEtuIII2bis;
    }

    public Boolean getReponseEtuIII3() {
        return reponseEtuIII3;
    }

    public void setReponseEtuIII3(Boolean reponseEtuIII3) {
        this.reponseEtuIII3 = reponseEtuIII3;
    }

    public String getReponseEtuIII3bis() {
        return reponseEtuIII3bis;
    }

    public void setReponseEtuIII3bis(String reponseEtuIII3bis) {
        this.reponseEtuIII3bis = reponseEtuIII3bis;
    }

    public Integer getReponseEtuIII4() {
        return reponseEtuIII4;
    }

    public void setReponseEtuIII4(Integer reponseEtuIII4) {
        this.reponseEtuIII4 = reponseEtuIII4;
    }

    public Boolean getReponseEtuIII5a() {
        return reponseEtuIII5a;
    }

    public void setReponseEtuIII5a(Boolean reponseEtuIII5a) {
        this.reponseEtuIII5a = reponseEtuIII5a;
    }

    public Boolean getReponseEtuIII5b() {
        return reponseEtuIII5b;
    }

    public void setReponseEtuIII5b(Boolean reponseEtuIII5b) {
        this.reponseEtuIII5b = reponseEtuIII5b;
    }

    public Boolean getReponseEtuIII5c() {
        return reponseEtuIII5c;
    }

    public void setReponseEtuIII5c(Boolean reponseEtuIII5c) {
        this.reponseEtuIII5c = reponseEtuIII5c;
    }

    public String getReponseEtuIII5bis() {
        return reponseEtuIII5bis;
    }

    public void setReponseEtuIII5bis(String reponseEtuIII5bis) {
        this.reponseEtuIII5bis = reponseEtuIII5bis;
    }

    public Integer getReponseEtuIII6() {
        return reponseEtuIII6;
    }

    public void setReponseEtuIII6(Integer reponseEtuIII6) {
        this.reponseEtuIII6 = reponseEtuIII6;
    }

    public String getReponseEtuIII6bis() {
        return reponseEtuIII6bis;
    }

    public void setReponseEtuIII6bis(String reponseEtuIII6bis) {
        this.reponseEtuIII6bis = reponseEtuIII6bis;
    }

    public Integer getReponseEtuIII7() {
        return reponseEtuIII7;
    }

    public void setReponseEtuIII7(Integer reponseEtuIII7) {
        this.reponseEtuIII7 = reponseEtuIII7;
    }

    public String getReponseEtuIII7bis() {
        return reponseEtuIII7bis;
    }

    public void setReponseEtuIII7bis(String reponseEtuIII7bis) {
        this.reponseEtuIII7bis = reponseEtuIII7bis;
    }

    public Boolean getReponseEtuIII8() {
        return reponseEtuIII8;
    }

    public void setReponseEtuIII8(Boolean reponseEtuIII8) {
        this.reponseEtuIII8 = reponseEtuIII8;
    }

    public String getReponseEtuIII8bis() {
        return reponseEtuIII8bis;
    }

    public void setReponseEtuIII8bis(String reponseEtuIII8bis) {
        this.reponseEtuIII8bis = reponseEtuIII8bis;
    }

    public Boolean getReponseEtuIII9() {
        return reponseEtuIII9;
    }

    public void setReponseEtuIII9(Boolean reponseEtuIII9) {
        this.reponseEtuIII9 = reponseEtuIII9;
    }

    public String getReponseEtuIII9bis() {
        return reponseEtuIII9bis;
    }

    public void setReponseEtuIII9bis(String reponseEtuIII9bis) {
        this.reponseEtuIII9bis = reponseEtuIII9bis;
    }

    public Boolean getReponseEtuIII10() {
        return reponseEtuIII10;
    }

    public void setReponseEtuIII10(Boolean reponseEtuIII10) {
        this.reponseEtuIII10 = reponseEtuIII10;
    }

    public Boolean getReponseEtuIII11() {
        return reponseEtuIII11;
    }

    public void setReponseEtuIII11(Boolean reponseEtuIII11) {
        this.reponseEtuIII11 = reponseEtuIII11;
    }

    public Boolean getReponseEtuIII12() {
        return reponseEtuIII12;
    }

    public void setReponseEtuIII12(Boolean reponseEtuIII12) {
        this.reponseEtuIII12 = reponseEtuIII12;
    }

    public Boolean getReponseEtuIII13() {
        return reponseEtuIII13;
    }

    public void setReponseEtuIII13(Boolean reponseEtuIII13) {
        this.reponseEtuIII13 = reponseEtuIII13;
    }

    public Boolean getReponseEtuIII14() {
        return reponseEtuIII14;
    }

    public void setReponseEtuIII14(Boolean reponseEtuIII14) {
        this.reponseEtuIII14 = reponseEtuIII14;
    }

    public Integer getReponseEtuIII15() {
        return reponseEtuIII15;
    }

    public void setReponseEtuIII15(Integer reponseEtuIII15) {
        this.reponseEtuIII15 = reponseEtuIII15;
    }

    public String getReponseEtuIII15bis() {
        return reponseEtuIII15bis;
    }

    public void setReponseEtuIII15bis(String reponseEtuIII15bis) {
        this.reponseEtuIII15bis = reponseEtuIII15bis;
    }

    public Integer getReponseEtuIII16() {
        return reponseEtuIII16;
    }

    public void setReponseEtuIII16(Integer reponseEtuIII16) {
        this.reponseEtuIII16 = reponseEtuIII16;
    }

    public String getReponseEtuIII16bis() {
        return reponseEtuIII16bis;
    }

    public void setReponseEtuIII16bis(String reponseEtuIII16bis) {
        this.reponseEtuIII16bis = reponseEtuIII16bis;
    }

    public Boolean getReponseEnsI1a() {
        return reponseEnsI1a;
    }

    public void setReponseEnsI1a(Boolean reponseEnsI1a) {
        this.reponseEnsI1a = reponseEnsI1a;
    }

    public Boolean getReponseEnsI1b() {
        return reponseEnsI1b;
    }

    public void setReponseEnsI1b(Boolean reponseEnsI1b) {
        this.reponseEnsI1b = reponseEnsI1b;
    }

    public Boolean getReponseEnsI1c() {
        return reponseEnsI1c;
    }

    public void setReponseEnsI1c(Boolean reponseEnsI1c) {
        this.reponseEnsI1c = reponseEnsI1c;
    }

    public Boolean getReponseEnsI2a() {
        return reponseEnsI2a;
    }

    public void setReponseEnsI2a(Boolean reponseEnsI2a) {
        this.reponseEnsI2a = reponseEnsI2a;
    }

    public Boolean getReponseEnsI2b() {
        return reponseEnsI2b;
    }

    public void setReponseEnsI2b(Boolean reponseEnsI2b) {
        this.reponseEnsI2b = reponseEnsI2b;
    }

    public Boolean getReponseEnsI2c() {
        return reponseEnsI2c;
    }

    public void setReponseEnsI2c(Boolean reponseEnsI2c) {
        this.reponseEnsI2c = reponseEnsI2c;
    }

    public String getReponseEnsI3() {
        return reponseEnsI3;
    }

    public void setReponseEnsI3(String reponseEnsI3) {
        this.reponseEnsI3 = reponseEnsI3;
    }

    public Integer getReponseEnsII1() {
        return reponseEnsII1;
    }

    public void setReponseEnsII1(Integer reponseEnsII1) {
        this.reponseEnsII1 = reponseEnsII1;
    }

    public Integer getReponseEnsII2() {
        return reponseEnsII2;
    }

    public void setReponseEnsII2(Integer reponseEnsII2) {
        this.reponseEnsII2 = reponseEnsII2;
    }

    public Integer getReponseEnsII3() {
        return reponseEnsII3;
    }

    public void setReponseEnsII3(Integer reponseEnsII3) {
        this.reponseEnsII3 = reponseEnsII3;
    }

    public Integer getReponseEnsII4() {
        return reponseEnsII4;
    }

    public void setReponseEnsII4(Integer reponseEnsII4) {
        this.reponseEnsII4 = reponseEnsII4;
    }

    public Integer getReponseEnsII5() {
        return reponseEnsII5;
    }

    public void setReponseEnsII5(Integer reponseEnsII5) {
        this.reponseEnsII5 = reponseEnsII5;
    }

    public Integer getReponseEnsII6() {
        return reponseEnsII6;
    }

    public void setReponseEnsII6(Integer reponseEnsII6) {
        this.reponseEnsII6 = reponseEnsII6;
    }

    public Integer getReponseEnsII7() {
        return reponseEnsII7;
    }

    public void setReponseEnsII7(Integer reponseEnsII7) {
        this.reponseEnsII7 = reponseEnsII7;
    }

    public Integer getReponseEnsII8() {
        return reponseEnsII8;
    }

    public void setReponseEnsII8(Integer reponseEnsII8) {
        this.reponseEnsII8 = reponseEnsII8;
    }

    public Integer getReponseEnsII9() {
        return reponseEnsII9;
    }

    public void setReponseEnsII9(Integer reponseEnsII9) {
        this.reponseEnsII9 = reponseEnsII9;
    }

    public Integer getReponseEnsII10() {
        return reponseEnsII10;
    }

    public void setReponseEnsII10(Integer reponseEnsII10) {
        this.reponseEnsII10 = reponseEnsII10;
    }

    public String getReponseEnsII11() {
        return reponseEnsII11;
    }

    public void setReponseEnsII11(String reponseEnsII11) {
        this.reponseEnsII11 = reponseEnsII11;
    }
}
