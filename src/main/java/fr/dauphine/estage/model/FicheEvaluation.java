package fr.dauphine.estage.model;

import javax.persistence.*;

@Entity
@Table(name = "FicheEvaluation")
public class FicheEvaluation {

    @Id
    @GeneratedValue
    @Column(name = "idFicheEvaluation", nullable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    private CentreGestion centreGestion;

    private Boolean validationEtudiant;
    private Boolean validationEnseignant;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CentreGestion getCentreGestion() {
        return centreGestion;
    }

    public void setCentreGestion(CentreGestion centreGestion) {
        this.centreGestion = centreGestion;
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

    public Boolean getQuestionEnt1() {
        return questionEnt1;
    }

    public void setQuestionEnt1(Boolean questionEnt1) {
        this.questionEnt1 = questionEnt1;
    }

    public Boolean getQuestionEnt2() {
        return questionEnt2;
    }

    public void setQuestionEnt2(Boolean questionEnt2) {
        this.questionEnt2 = questionEnt2;
    }

    public Boolean getQuestionEnt3() {
        return questionEnt3;
    }

    public void setQuestionEnt3(Boolean questionEnt3) {
        this.questionEnt3 = questionEnt3;
    }

    public Boolean getQuestionEnt4() {
        return questionEnt4;
    }

    public void setQuestionEnt4(Boolean questionEnt4) {
        this.questionEnt4 = questionEnt4;
    }

    public Boolean getQuestionEnt5() {
        return questionEnt5;
    }

    public void setQuestionEnt5(Boolean questionEnt5) {
        this.questionEnt5 = questionEnt5;
    }

    public Boolean getQuestionEnt6() {
        return questionEnt6;
    }

    public void setQuestionEnt6(Boolean questionEnt6) {
        this.questionEnt6 = questionEnt6;
    }

    public Boolean getQuestionEnt7() {
        return questionEnt7;
    }

    public void setQuestionEnt7(Boolean questionEnt7) {
        this.questionEnt7 = questionEnt7;
    }

    public Boolean getQuestionEnt8() {
        return questionEnt8;
    }

    public void setQuestionEnt8(Boolean questionEnt8) {
        this.questionEnt8 = questionEnt8;
    }

    public Boolean getQuestionEnt9() {
        return questionEnt9;
    }

    public void setQuestionEnt9(Boolean questionEnt9) {
        this.questionEnt9 = questionEnt9;
    }

    public Boolean getQuestionEnt10() {
        return questionEnt10;
    }

    public void setQuestionEnt10(Boolean questionEnt10) {
        this.questionEnt10 = questionEnt10;
    }

    public Boolean getQuestionEnt11() {
        return questionEnt11;
    }

    public void setQuestionEnt11(Boolean questionEnt11) {
        this.questionEnt11 = questionEnt11;
    }

    public Boolean getQuestionEnt12() {
        return questionEnt12;
    }

    public void setQuestionEnt12(Boolean questionEnt12) {
        this.questionEnt12 = questionEnt12;
    }

    public Boolean getQuestionEnt13() {
        return questionEnt13;
    }

    public void setQuestionEnt13(Boolean questionEnt13) {
        this.questionEnt13 = questionEnt13;
    }

    public Boolean getQuestionEnt14() {
        return questionEnt14;
    }

    public void setQuestionEnt14(Boolean questionEnt14) {
        this.questionEnt14 = questionEnt14;
    }

    public Boolean getQuestionEnt15() {
        return questionEnt15;
    }

    public void setQuestionEnt15(Boolean questionEnt15) {
        this.questionEnt15 = questionEnt15;
    }

    public Boolean getQuestionEnt16() {
        return questionEnt16;
    }

    public void setQuestionEnt16(Boolean questionEnt16) {
        this.questionEnt16 = questionEnt16;
    }

    public Boolean getQuestionEnt17() {
        return questionEnt17;
    }

    public void setQuestionEnt17(Boolean questionEnt17) {
        this.questionEnt17 = questionEnt17;
    }

    public Boolean getQuestionEnt18() {
        return questionEnt18;
    }

    public void setQuestionEnt18(Boolean questionEnt18) {
        this.questionEnt18 = questionEnt18;
    }

    public Boolean getQuestionEnt19() {
        return questionEnt19;
    }

    public void setQuestionEnt19(Boolean questionEnt19) {
        this.questionEnt19 = questionEnt19;
    }

    public Boolean getQuestionEtuI1() {
        return questionEtuI1;
    }

    public void setQuestionEtuI1(Boolean questionEtuI1) {
        this.questionEtuI1 = questionEtuI1;
    }

    public Boolean getQuestionEtuI2() {
        return questionEtuI2;
    }

    public void setQuestionEtuI2(Boolean questionEtuI2) {
        this.questionEtuI2 = questionEtuI2;
    }

    public Boolean getQuestionEtuI3() {
        return questionEtuI3;
    }

    public void setQuestionEtuI3(Boolean questionEtuI3) {
        this.questionEtuI3 = questionEtuI3;
    }

    public Boolean getQuestionEtuI4() {
        return questionEtuI4;
    }

    public void setQuestionEtuI4(Boolean questionEtuI4) {
        this.questionEtuI4 = questionEtuI4;
    }

    public Boolean getQuestionEtuI5() {
        return questionEtuI5;
    }

    public void setQuestionEtuI5(Boolean questionEtuI5) {
        this.questionEtuI5 = questionEtuI5;
    }

    public Boolean getQuestionEtuI6() {
        return questionEtuI6;
    }

    public void setQuestionEtuI6(Boolean questionEtuI6) {
        this.questionEtuI6 = questionEtuI6;
    }

    public Boolean getQuestionEtuI7() {
        return questionEtuI7;
    }

    public void setQuestionEtuI7(Boolean questionEtuI7) {
        this.questionEtuI7 = questionEtuI7;
    }

    public Boolean getQuestionEtuI8() {
        return questionEtuI8;
    }

    public void setQuestionEtuI8(Boolean questionEtuI8) {
        this.questionEtuI8 = questionEtuI8;
    }

    public Boolean getQuestionEtuII1() {
        return questionEtuII1;
    }

    public void setQuestionEtuII1(Boolean questionEtuII1) {
        this.questionEtuII1 = questionEtuII1;
    }

    public Boolean getQuestionEtuII2() {
        return questionEtuII2;
    }

    public void setQuestionEtuII2(Boolean questionEtuII2) {
        this.questionEtuII2 = questionEtuII2;
    }

    public Boolean getQuestionEtuII3() {
        return questionEtuII3;
    }

    public void setQuestionEtuII3(Boolean questionEtuII3) {
        this.questionEtuII3 = questionEtuII3;
    }

    public Boolean getQuestionEtuII4() {
        return questionEtuII4;
    }

    public void setQuestionEtuII4(Boolean questionEtuII4) {
        this.questionEtuII4 = questionEtuII4;
    }

    public Boolean getQuestionEtuII5() {
        return questionEtuII5;
    }

    public void setQuestionEtuII5(Boolean questionEtuII5) {
        this.questionEtuII5 = questionEtuII5;
    }

    public Boolean getQuestionEtuII6() {
        return questionEtuII6;
    }

    public void setQuestionEtuII6(Boolean questionEtuII6) {
        this.questionEtuII6 = questionEtuII6;
    }

    public Boolean getQuestionEtuIII1() {
        return questionEtuIII1;
    }

    public void setQuestionEtuIII1(Boolean questionEtuIII1) {
        this.questionEtuIII1 = questionEtuIII1;
    }

    public Boolean getQuestionEtuIII2() {
        return questionEtuIII2;
    }

    public void setQuestionEtuIII2(Boolean questionEtuIII2) {
        this.questionEtuIII2 = questionEtuIII2;
    }

    public Boolean getQuestionEtuIII3() {
        return questionEtuIII3;
    }

    public void setQuestionEtuIII3(Boolean questionEtuIII3) {
        this.questionEtuIII3 = questionEtuIII3;
    }

    public Boolean getQuestionEtuIII4() {
        return questionEtuIII4;
    }

    public void setQuestionEtuIII4(Boolean questionEtuIII4) {
        this.questionEtuIII4 = questionEtuIII4;
    }

    public Boolean getQuestionEtuIII5() {
        return questionEtuIII5;
    }

    public void setQuestionEtuIII5(Boolean questionEtuIII5) {
        this.questionEtuIII5 = questionEtuIII5;
    }

    public Boolean getQuestionEtuIII6() {
        return questionEtuIII6;
    }

    public void setQuestionEtuIII6(Boolean questionEtuIII6) {
        this.questionEtuIII6 = questionEtuIII6;
    }

    public Boolean getQuestionEtuIII7() {
        return questionEtuIII7;
    }

    public void setQuestionEtuIII7(Boolean questionEtuIII7) {
        this.questionEtuIII7 = questionEtuIII7;
    }

    public Boolean getQuestionEtuIII8() {
        return questionEtuIII8;
    }

    public void setQuestionEtuIII8(Boolean questionEtuIII8) {
        this.questionEtuIII8 = questionEtuIII8;
    }

    public Boolean getQuestionEtuIII9() {
        return questionEtuIII9;
    }

    public void setQuestionEtuIII9(Boolean questionEtuIII9) {
        this.questionEtuIII9 = questionEtuIII9;
    }

    public Boolean getQuestionEtuIII10() {
        return questionEtuIII10;
    }

    public void setQuestionEtuIII10(Boolean questionEtuIII10) {
        this.questionEtuIII10 = questionEtuIII10;
    }

    public Boolean getQuestionEtuIII11() {
        return questionEtuIII11;
    }

    public void setQuestionEtuIII11(Boolean questionEtuIII11) {
        this.questionEtuIII11 = questionEtuIII11;
    }

    public Boolean getQuestionEtuIII12() {
        return questionEtuIII12;
    }

    public void setQuestionEtuIII12(Boolean questionEtuIII12) {
        this.questionEtuIII12 = questionEtuIII12;
    }

    public Boolean getQuestionEtuIII13() {
        return questionEtuIII13;
    }

    public void setQuestionEtuIII13(Boolean questionEtuIII13) {
        this.questionEtuIII13 = questionEtuIII13;
    }

    public Boolean getQuestionEtuIII14() {
        return questionEtuIII14;
    }

    public void setQuestionEtuIII14(Boolean questionEtuIII14) {
        this.questionEtuIII14 = questionEtuIII14;
    }

    public Boolean getQuestionEtuIII15() {
        return questionEtuIII15;
    }

    public void setQuestionEtuIII15(Boolean questionEtuIII15) {
        this.questionEtuIII15 = questionEtuIII15;
    }

    public Boolean getQuestionEtuIII16() {
        return questionEtuIII16;
    }

    public void setQuestionEtuIII16(Boolean questionEtuIII16) {
        this.questionEtuIII16 = questionEtuIII16;
    }

    public Boolean getQuestionEnsI1() {
        return questionEnsI1;
    }

    public void setQuestionEnsI1(Boolean questionEnsI1) {
        this.questionEnsI1 = questionEnsI1;
    }

    public Boolean getQuestionEnsI2() {
        return questionEnsI2;
    }

    public void setQuestionEnsI2(Boolean questionEnsI2) {
        this.questionEnsI2 = questionEnsI2;
    }

    public Boolean getQuestionEnsI3() {
        return questionEnsI3;
    }

    public void setQuestionEnsI3(Boolean questionEnsI3) {
        this.questionEnsI3 = questionEnsI3;
    }

    public Boolean getQuestionEnsII1() {
        return questionEnsII1;
    }

    public void setQuestionEnsII1(Boolean questionEnsII1) {
        this.questionEnsII1 = questionEnsII1;
    }

    public Boolean getQuestionEnsII2() {
        return questionEnsII2;
    }

    public void setQuestionEnsII2(Boolean questionEnsII2) {
        this.questionEnsII2 = questionEnsII2;
    }

    public Boolean getQuestionEnsII3() {
        return questionEnsII3;
    }

    public void setQuestionEnsII3(Boolean questionEnsII3) {
        this.questionEnsII3 = questionEnsII3;
    }

    public Boolean getQuestionEnsII4() {
        return questionEnsII4;
    }

    public void setQuestionEnsII4(Boolean questionEnsII4) {
        this.questionEnsII4 = questionEnsII4;
    }

    public Boolean getQuestionEnsII5() {
        return questionEnsII5;
    }

    public void setQuestionEnsII5(Boolean questionEnsII5) {
        this.questionEnsII5 = questionEnsII5;
    }

    public Boolean getQuestionEnsII6() {
        return questionEnsII6;
    }

    public void setQuestionEnsII6(Boolean questionEnsII6) {
        this.questionEnsII6 = questionEnsII6;
    }

    public Boolean getQuestionEnsII7() {
        return questionEnsII7;
    }

    public void setQuestionEnsII7(Boolean questionEnsII7) {
        this.questionEnsII7 = questionEnsII7;
    }

    public Boolean getQuestionEnsII8() {
        return questionEnsII8;
    }

    public void setQuestionEnsII8(Boolean questionEnsII8) {
        this.questionEnsII8 = questionEnsII8;
    }

    public Boolean getQuestionEnsII9() {
        return questionEnsII9;
    }

    public void setQuestionEnsII9(Boolean questionEnsII9) {
        this.questionEnsII9 = questionEnsII9;
    }

    public Boolean getQuestionEnsII10() {
        return questionEnsII10;
    }

    public void setQuestionEnsII10(Boolean questionEnsII10) {
        this.questionEnsII10 = questionEnsII10;
    }

    public Boolean getQuestionEnsII11() {
        return questionEnsII11;
    }

    public void setQuestionEnsII11(Boolean questionEnsII11) {
        this.questionEnsII11 = questionEnsII11;
    }
}
