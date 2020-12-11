package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the UniteDuree database table.
 *
 */
@Entity
@Table(name = "UniteDuree")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "UniteDuree.findAll", query = "SELECT u FROM UniteDuree u")
public class UniteDuree implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idUniteDuree;
    @Column(nullable = false, length = 100)
    private String libelleUniteDuree;
    @Column(nullable = false, length = 1)
    private String temEnServUniteDuree;
    // bi-directional many-to-one association to Convention
    @OneToMany(mappedBy = "uniteDuree")
    private List<Convention> conventions;
    // bi-directional many-to-one association to Offre
    @OneToMany(mappedBy = "uniteDuree")
    private List<Offre> offres;



    public Convention addConvention(Convention convention) {
        getConventions().add(convention);
        convention.setUniteDuree(this);
        return convention;
    }

    public Convention removeConvention(Convention convention) {
        getConventions().remove(convention);
        convention.setUniteDuree(null);
        return convention;
    }


    public Offre addOffre(Offre offre) {
        getOffres().add(offre);
        offre.setUniteDuree(this);
        return offre;
    }

    public Offre removeOffre(Offre offre) {
        getOffres().remove(offre);
        offre.setUniteDuree(null);
        return offre;
    }
}