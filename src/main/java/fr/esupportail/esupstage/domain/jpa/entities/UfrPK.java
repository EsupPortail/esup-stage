package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the Ufr database table.
 *
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class UfrPK implements Serializable {
    // default serial version id, required for serializable classes.
    private static final long serialVersionUID = 1L;
    @Column(unique = true, nullable = false, length = 10)
    private String codeUFR;
    @Column(unique = true, nullable = false, length = 50)
    private String codeUniversite;


    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UfrPK)) {
            return false;
        }
        UfrPK castOther = (UfrPK) other;
        return this.codeUFR.equals(castOther.codeUFR) && this.codeUniversite.equals(castOther.codeUniversite);
    }

    public int hashCode() {
        final Integer prime = 31;
        Integer hash = 17;
        hash = hash * prime + this.codeUFR.hashCode();
        hash = hash * prime + this.codeUniversite.hashCode();
        return hash;
    }
}