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

import org.hibernate.annotations.GenericGenerator;

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
    @Column(name = "idIndemnisation")
    @GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
    private Integer id;
    @Column(name = "libelleIndemnisation", nullable = false, length = 50)
    private String label;
    @Column(name = "temEnServIndem", nullable = false, length = 1)
    private String temEnServ;
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