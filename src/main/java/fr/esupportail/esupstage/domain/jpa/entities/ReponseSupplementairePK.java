package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the ReponseSupplementaire database table.
 *
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ReponseSupplementairePK implements Serializable {
    // default serial version id, required for serializable classes.
    private static final long serialVersionUID = 1L;
    @Column(insertable = false, updatable = false, unique = true, nullable = false)
    private Integer idQuestionSupplementaire;
    @Column(insertable = false, updatable = false, unique = true, nullable = false)
    private Integer idConvention;

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ReponseSupplementairePK)) {
            return false;
        }
        ReponseSupplementairePK castOther = (ReponseSupplementairePK) other;
        return (this.idQuestionSupplementaire == castOther.idQuestionSupplementaire)
                && (this.idConvention == castOther.idConvention);
    }

    public int hashCode() {
        final Integer prime = 31;
        Integer hash = 17;
        hash = hash * prime + this.idQuestionSupplementaire;
        hash = hash * prime + this.idConvention;
        return hash;
    }
}