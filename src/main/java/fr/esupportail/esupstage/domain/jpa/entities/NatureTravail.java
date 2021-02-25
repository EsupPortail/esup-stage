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
 * The persistent class for the NatureTravail database table.
 *
 */
@Entity
@Table(name = "NatureTravail")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "NatureTravail.findAll", query = "SELECT n FROM NatureTravail n")
public class NatureTravail implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "idNatureTravail")
    @GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
    private Integer id;
    @Column(name = "libelleNatureTravail", nullable = false, length = 150)
    private String label;
    @Column(name = "temEnServNatTrav", nullable = false, length = 1)
    private String temEnServ;
    // bi-directional many-to-one association to Convention
    @OneToMany(mappedBy = "natureTravail")
    private List<Convention> conventions;

    public Convention addConvention(Convention convention) {
        getConventions().add(convention);
        convention.setNatureTravail(this);
        return convention;
    }

    public Convention removeConvention(Convention convention) {
        getConventions().remove(convention);
        convention.setNatureTravail(null);
        return convention;
    }
}