package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the OffreDiffusion database table.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class OffreDiffusionPK implements Serializable {
    // default serial version id, required for serializable classes.
    private static final long serialVersionUID = 1L;
    @Column(insertable = false, updatable = false, unique = true, nullable = false)
    private Integer idOffre;
    @Column(insertable = false, updatable = false, unique = true, nullable = false)
    private Integer idCentreGestion;

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OffreDiffusionPK)) {
            return false;
        }
        OffreDiffusionPK castOther = (OffreDiffusionPK) other;
        return (this.idOffre == castOther.idOffre) && (this.idCentreGestion == castOther.idCentreGestion);
    }

    public int hashCode() {
        final Integer prime = 31;
        Integer hash = 17;
        hash = hash * prime + this.idOffre;
        hash = hash * prime + this.idCentreGestion;
        return hash;
    }
}
