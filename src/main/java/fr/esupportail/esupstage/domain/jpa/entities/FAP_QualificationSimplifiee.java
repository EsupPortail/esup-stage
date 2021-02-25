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
    @Column(name = "idQualificationSimplifiee")
    @GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
    private Integer id;
    @Column(name = "libelleQualification", length = 100)
    private String label;
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