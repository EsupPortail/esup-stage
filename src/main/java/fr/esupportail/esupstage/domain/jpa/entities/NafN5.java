package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the NAF_N5 database table.
 *
 */
@Entity
@Table(name = "NAF_N5")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "NafN5.findAll", query = "SELECT n FROM NafN5 n")
public class NafN5 implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(unique = true, nullable = false, length = 6)
    private String codeNAF_N5;
    @Column(length = 150)
    private String libelleNAF_N5;
    @Column(nullable = false, length = 1)
    private String temEnServNAF_N5;
    // bi-directional many-to-one association to NafN1
    @ManyToOne
    @JoinColumn(name = "codeNAF_N1", nullable = false)
    private NafN1 nafN1;
    // bi-directional many-to-one association to Structure
    @OneToMany(mappedBy = "nafN5")
    private List<Structure> structures;

    public Structure addStructure(Structure structure) {
        getStructures().add(structure);
        structure.setNafN5(this);
        return structure;
    }

    public Structure removeStructure(Structure structure) {
        getStructures().remove(structure);
        structure.setNafN5(null);
        return structure;
    }
}
