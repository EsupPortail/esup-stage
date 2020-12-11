package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the Ufr database table.
 *
 */
@Entity
@Table(name = "Ufr")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "Ufr.findAll", query = "SELECT u FROM Ufr u")
public class Ufr implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    private UfrPK id;
    @Column(nullable = false, length = 100)
    private String libelleUFR;
    // bi-directional many-to-one association to Convention
    @OneToMany(mappedBy = "ufr")
    private List<Convention> conventions;


    public Convention addConvention(Convention convention) {
        getConventions().add(convention);
        convention.setUfr(this);
        return convention;
    }

    public Convention removeConvention(Convention convention) {
        getConventions().remove(convention);
        convention.setUfr(null);
        return convention;
    }
}