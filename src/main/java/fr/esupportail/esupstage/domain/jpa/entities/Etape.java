package fr.esupportail.esupstage.domain.jpa.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the Etape database table.
 *
 */
@Entity
@Table(name = "Etape")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "Etape.findAll", query = "SELECT e FROM Etape e")
public class Etape implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    private EtapePK id;
    @Column(nullable = false, length = 200)
    private String libelleEtape;
    // bi-directional many-to-one association to Convention
    @OneToMany(mappedBy = "etape")
    private List<Convention> conventions;

    public Convention addConvention(Convention convention) {
        getConventions().add(convention);
        convention.setEtape(this);
        return convention;
    }

    public Convention removeConvention(Convention convention) {
        getConventions().remove(convention);
        convention.setEtape(null);
        return convention;
    }
}