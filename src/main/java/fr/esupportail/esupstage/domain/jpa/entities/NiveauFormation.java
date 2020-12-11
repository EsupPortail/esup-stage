package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the NiveauFormation database table.
 *
 */
@Entity
@Table(name = "NiveauFormation")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "NiveauFormation.findAll", query = "SELECT n FROM NiveauFormation n")
public class NiveauFormation implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idNiveauFormation;
    @Column(nullable = false, length = 45)
    private String libelleNiveauFormation;
    private boolean modifiable;
    @Column(nullable = false, length = 1)
    private String temEnServNiveauForm;
    // bi-directional many-to-one association to Offre
    @OneToMany(mappedBy = "niveauFormation")
    private List<Offre> offres;

    public Offre addOffre(Offre offre) {
        getOffres().add(offre);
        offre.setNiveauFormation(this);
        return offre;
    }

    public Offre removeOffre(Offre offre) {
        getOffres().remove(offre);
        offre.setNiveauFormation(null);
        return offre;
    }
}