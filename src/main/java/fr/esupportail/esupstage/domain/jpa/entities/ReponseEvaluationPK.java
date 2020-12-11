package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the ReponseEvaluation database table.
 *
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ReponseEvaluationPK implements Serializable {
    // default serial version id, required for serializable classes.
    private static final long serialVersionUID = 1L;
    @Column(insertable = false, updatable = false, unique = true, nullable = false)
    private Integer idFicheEvaluation;
    @Column(insertable = false, updatable = false, unique = true, nullable = false)
    private Integer idConvention;

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ReponseEvaluationPK)) {
            return false;
        }
        ReponseEvaluationPK castOther = (ReponseEvaluationPK) other;
        return (this.idFicheEvaluation == castOther.idFicheEvaluation) && (this.idConvention == castOther.idConvention);
    }

    public int hashCode() {
        final Integer prime = 31;
        Integer hash = 17;
        hash = hash * prime + this.idFicheEvaluation;
        hash = hash * prime + this.idConvention;
        return hash;
    }
}