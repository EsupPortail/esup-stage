package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the Effectif database table.
 *
 */
@Entity
@Table(name = "Effectif")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "Effectif.findAll", query = "SELECT e FROM Effectif e")
public class Effectif implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idEffectif;
    @Column(nullable = false, length = 100)
    private String libelleEffectif;
    private boolean modifiable;
    @Column(nullable = false, length = 1)
    private String temEnServEffectif;
    // bi-directional many-to-one association to Structure
    @OneToMany(mappedBy = "effectif")
    private List<Structure> structures;

    public Structure addStructure(Structure structure) {
        getStructures().add(structure);
        structure.setEffectif(this);
        return structure;
    }

    public Structure removeStructure(Structure structure) {
        getStructures().remove(structure);
        structure.setEffectif(null);
        return structure;
    }
}
