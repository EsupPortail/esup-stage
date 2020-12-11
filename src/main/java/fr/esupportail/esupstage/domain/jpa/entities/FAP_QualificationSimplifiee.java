package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the FAP_QualificationSimplifiee database table.
 *
 */
@Entity
@Table(name = "FAP_QualificationSimplifiee")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "FAP_QualificationSimplifiee.findAll", query = "SELECT f FROM FAP_QualificationSimplifiee f")
public class FAP_QualificationSimplifiee implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idQualificationSimplifiee;
    @Column(length = 100)
    private String libelleQualification;
    // bi-directional many-to-one association to FAP_Qualification
    @OneToMany(mappedBy = "fapQualificationSimplifiee")
    private List<FAP_Qualification> fapQualifications;
    // bi-directional many-to-one association to Offre
    @OneToMany(mappedBy = "fapQualificationSimplifiee")
    private List<Offre> offres;

    public FAP_Qualification addFapQualification(FAP_Qualification fapQualification) {
        getFapQualifications().add(fapQualification);
        fapQualification.setFapQualificationSimplifiee(this);
        return fapQualification;
    }

    public FAP_Qualification removeFapQualification(FAP_Qualification fapQualification) {
        getFapQualifications().remove(fapQualification);
        fapQualification.setFapQualificationSimplifiee(null);
        return fapQualification;
    }

    public Offre addOffre(Offre offre) {
        getOffres().add(offre);
        offre.setFapQualificationSimplifiee(this);
        return offre;
    }

    public Offre removeOffre(Offre offre) {
        getOffres().remove(offre);
        offre.setFapQualificationSimplifiee(null);
        return offre;
    }
}