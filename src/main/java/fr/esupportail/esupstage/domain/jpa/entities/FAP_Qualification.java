package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the FAP_Qualification database table.
 *
 */
@Entity
@Table(name = "FAP_Qualification")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "FAP_Qualification.findAll", query = "SELECT f FROM FAP_Qualification f")
public class FAP_Qualification implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer numFAP_Qualification;
    @Column(nullable = false, length = 100)
    private String libelleQualification;
    // bi-directional many-to-one association to FapN3
    @OneToMany(mappedBy = "fapQualification")
    private List<FapN3> fapN3s;
    // bi-directional many-to-one association to FAP_QualificationSimplifiee
    @ManyToOne
    @JoinColumn(name = "idQualificationSimplifiee", nullable = false)
    private FAP_QualificationSimplifiee fapQualificationSimplifiee;

}