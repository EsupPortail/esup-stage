package org.esup_portail.esup_stage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class ReponseSupplementaireId implements Serializable {

    @Column(nullable = false)
    private int idQuestionSupplementaire;

    @Column(nullable = false)
    private int idConvention;

    public int getIdQuestionSupplementaire() {
        return idQuestionSupplementaire;
    }

    public void setIdQuestionSupplementaire(int idQuestionSupplementaire) {
        this.idQuestionSupplementaire = idQuestionSupplementaire;
    }

    public int getIdConvention() {
        return idConvention;
    }

    public void setIdConvention(int idConvention) {
        this.idConvention = idConvention;
    }
}
