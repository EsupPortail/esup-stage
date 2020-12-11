package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the CritereGestion database table.
 *
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class CritereGestionPK implements Serializable {
    // default serial version id, required for serializable classes.
    private static final long serialVersionUID = 1L;
    @Column(unique = true, nullable = false, length = 15)
    private String codeCritere;
    @Column(unique = true, nullable = false, length = 10)
    private String codeVersionEtape;


    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CritereGestionPK)) {
            return false;
        }
        CritereGestionPK castOther = (CritereGestionPK) other;
        return this.codeCritere.equals(castOther.codeCritere)
                && this.codeVersionEtape.equals(castOther.codeVersionEtape);
    }

    public int hashCode() {
        final Integer prime = 31;
        Integer hash = 17;
        hash = hash * prime + this.codeCritere.hashCode();
        hash = hash * prime + this.codeVersionEtape.hashCode();
        return hash;
    }
}