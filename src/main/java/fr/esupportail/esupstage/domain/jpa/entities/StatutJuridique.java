package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the StatutJuridique database table.
 *
 */
@Entity
@Table(name = "StatutJuridique")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "StatutJuridique.findAll", query = "SELECT s FROM StatutJuridique s")
public class StatutJuridique implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idStatutJuridique;
    @Column(nullable = false, length = 100)
    private String libelleStatutJuridique;
    private boolean modifiable;
    @Column(nullable = false, length = 1)
    private String temEnServStatut;
    // bi-directional many-to-one association to TypeStructure
    @ManyToOne
    @JoinColumn(name = "idTypeStructure", nullable = false)
    private TypeStructure typeStructure;
    // bi-directional many-to-one association to Structure
    @OneToMany(mappedBy = "statutJuridique")
    private List<Structure> structures;

    public Structure addStructure(Structure structure) {
        getStructures().add(structure);
        structure.setStatutJuridique(this);
        return structure;
    }

    public Structure removeStructure(Structure structure) {
        getStructures().remove(structure);
        structure.setStatutJuridique(null);
        return structure;
    }
}