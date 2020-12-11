package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the TypeOffre database table.
 *
 */
@Entity
@Table(name = "TypeOffre")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "TypeOffre.findAll", query = "SELECT t FROM TypeOffre t")
public class TypeOffre implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idTypeOffre;
    @Column(nullable = false, length = 20)
    private String codeCtrl;
    @Column(nullable = false, length = 50)
    private String libelleType;
    private boolean modifiable;
    @Column(nullable = false, length = 1)
    private String temEnServTypeOffre;
    // bi-directional many-to-one association to ContratOffre
    @OneToMany(mappedBy = "typeOffre")
    private List<ContratOffre> contratOffres;
    // bi-directional many-to-one association to Offre
    @OneToMany(mappedBy = "typeOffre")
    private List<Offre> offres;


    public ContratOffre addContratOffre(ContratOffre contratOffre) {
        getContratOffres().add(contratOffre);
        contratOffre.setTypeOffre(this);
        return contratOffre;
    }

    public ContratOffre removeContratOffre(ContratOffre contratOffre) {
        getContratOffres().remove(contratOffre);
        contratOffre.setTypeOffre(null);
        return contratOffre;
    }


    public Offre addOffre(Offre offre) {
        getOffres().add(offre);
        offre.setTypeOffre(this);
        return offre;
    }

    public Offre removeOffre(Offre offre) {
        getOffres().remove(offre);
        offre.setTypeOffre(null);
        return offre;
    }
}