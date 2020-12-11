package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the TypeConvention database table.
 *
 */
@Entity
@Table(name = "TypeConvention")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "TypeConvention.findAll", query = "SELECT t FROM TypeConvention t")
public class TypeConvention implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idTypeConvention;
    @Column(nullable = false, length = 20)
    private String codeCtrl;
    @Column(nullable = false, length = 50)
    private String libelleTypeConvention;
    private boolean modifiable;
    @Column(nullable = false, length = 1)
    private String temEnServTypeConvention;
    // bi-directional many-to-one association to Convention
    @OneToMany(mappedBy = "typeConvention")
    private List<Convention> conventions;

    public Convention addConvention(Convention convention) {
        getConventions().add(convention);
        convention.setTypeConvention(this);
        return convention;
    }

    public Convention removeConvention(Convention convention) {
        getConventions().remove(convention);
        convention.setTypeConvention(null);
        return convention;
    }
}