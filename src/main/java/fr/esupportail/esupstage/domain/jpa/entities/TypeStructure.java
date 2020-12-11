package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the TypeStructure database table.
 *
 */
@Entity
@Table(name = "TypeStructure")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "TypeStructure.findAll", query = "SELECT t FROM TypeStructure t")
public class TypeStructure implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idTypeStructure;
    @Column(nullable = false, length = 100)
    private String libelleTypeStructure;
    private boolean modifiable;
    @Column(nullable = false)
    private boolean siretObligatoire;
    @Column(nullable = false, length = 1)
    private String temEnServTypeStructure;
    // bi-directional many-to-one association to StatutJuridique
    @OneToMany(mappedBy = "typeStructure")
    private List<StatutJuridique> statutJuridiques;
    // bi-directional many-to-one association to Structure
    @OneToMany(mappedBy = "typeStructure")
    private List<Structure> structures;

    public StatutJuridique addStatutJuridique(StatutJuridique statutJuridique) {
        getStatutJuridiques().add(statutJuridique);
        statutJuridique.setTypeStructure(this);
        return statutJuridique;
    }

    public StatutJuridique removeStatutJuridique(StatutJuridique statutJuridique) {
        getStatutJuridiques().remove(statutJuridique);
        statutJuridique.setTypeStructure(null);
        return statutJuridique;
    }

    public Structure addStructure(Structure structure) {
        getStructures().add(structure);
        structure.setTypeStructure(this);
        return structure;
    }

    public Structure removeStructure(Structure structure) {
        getStructures().remove(structure);
        structure.setTypeStructure(null);
        return structure;
    }
}