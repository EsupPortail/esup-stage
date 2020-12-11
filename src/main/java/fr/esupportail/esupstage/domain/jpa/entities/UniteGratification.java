package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the UniteGratification database table.
 *
 */
@Entity
@Table(name = "UniteGratification")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "UniteGratification.findAll", query = "SELECT u FROM UniteGratification u")
public class UniteGratification implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idUniteGratification;
    @Column(nullable = false, length = 50)
    private String libelleUniteGratification;
    @Column(nullable = false, length = 1)
    private String temEnServGrat;
    // bi-directional many-to-one association to Convention
    @OneToMany(mappedBy = "uniteGratification")
    private List<Convention> conventions;

    public Convention addConvention(Convention convention) {
        getConventions().add(convention);
        convention.setUniteGratification(this);
        return convention;
    }

    public Convention removeConvention(Convention convention) {
        getConventions().remove(convention);
        convention.setUniteGratification(null);
        return convention;
    }
}