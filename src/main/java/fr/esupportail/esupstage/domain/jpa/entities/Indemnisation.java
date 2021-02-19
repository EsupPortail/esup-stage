package fr.esupportail.esupstage.domain.jpa.entities;


import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the Indemnisation database table.
 *
 */
@Entity
@Table(name = "Indemnisation")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "Indemnisation.findAll", query = "SELECT i FROM Indemnisation i")
public class Indemnisation implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idIndemnisation;
    @Column(nullable = false, length = 50)
    private String libelleIndemnisation;
    @Column(nullable = false, length = 1)
    private String temEnServIndem;
    // bi-directional many-to-one association to Convention
    @OneToMany(mappedBy = "indemnisation")
    private List<Convention> conventions;

    public Convention addConvention(Convention convention) {
        getConventions().add(convention);
        convention.setIndemnisation(this);
        return convention;
    }

    public Convention removeConvention(Convention convention) {
        getConventions().remove(convention);
        convention.setIndemnisation(null);
        return convention;
    }
}