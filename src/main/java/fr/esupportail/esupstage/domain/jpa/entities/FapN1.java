package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the FAP_N1 database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "FAP_N1")
@NamedQuery(name = "FapN1.findAll", query = "SELECT f FROM FapN1 f")
public class FapN1 implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(unique = true, nullable = false, length = 1)
    private String codeFAP_N1;
    @Column(nullable = false, length = 200)
    private String libelle;
    // bi-directional many-to-one association to FapN2
    @OneToMany(mappedBy = "fapN1")
    private List<FapN2> fapN2s;
    // bi-directional many-to-one association to Offre
    @OneToMany(mappedBy = "fapN1")
    private List<Offre> offres;

    public FapN2 addFapN2(FapN2 fapN2) {
        getFapN2s().add(fapN2);
        fapN2.setFapN1(this);
        return fapN2;
    }

    public FapN2 removeFapN2(FapN2 fapN2) {
        getFapN2s().remove(fapN2);
        fapN2.setFapN1(null);
        return fapN2;
    }

    public Offre addOffre(Offre offre) {
        getOffres().add(offre);
        offre.setFapN1(this);
        return offre;
    }

    public Offre removeOffre(Offre offre) {
        getOffres().remove(offre);
        offre.setFapN1(null);
        return offre;
    }
}