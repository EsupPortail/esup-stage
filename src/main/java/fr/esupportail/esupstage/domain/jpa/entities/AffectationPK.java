package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the Affectation database table.
 *
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class AffectationPK implements Serializable {
    // default serial version id, required for serializable classes.
    private static final long serialVersionUID = 1L;
    @Column(unique = true, nullable = false, length = 10)
    private String codeAffectation;
    @Column(unique = true, nullable = false, length = 50)
    private String codeUniversite;


    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AffectationPK)) {
            return false;
        }
        AffectationPK castOther = (AffectationPK) other;
        return this.codeAffectation.equals(castOther.codeAffectation)
                && this.codeUniversite.equals(castOther.codeUniversite);
    }

    public int hashCode() {
        final Integer prime = 31;
        Integer hash = 17;
        hash = hash * prime + this.codeAffectation.hashCode();
        hash = hash * prime + this.codeUniversite.hashCode();
        return hash;
    }
}