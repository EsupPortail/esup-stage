package fr.esupportail.esupstage.domain.jpa.entities;


import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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