package org.esup_portail.esup_stage.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ReponseEvaluationId implements Serializable {

    @Column(nullable = false)
    private int idFicheEvaluation;

    @Column(nullable = false)
    private int idConvention;

    public int getIdFicheEvaluation() {
        return idFicheEvaluation;
    }

    public void setIdFicheEvaluation(int idFicheEvaluation) {
        this.idFicheEvaluation = idFicheEvaluation;
    }

    public int getIdConvention() {
        return idConvention;
    }

    public void setIdConvention(int idConvention) {
        this.idConvention = idConvention;
    }
}
