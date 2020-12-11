package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the NiveauCentre database table.
 *
 */
@Entity
@Table(name = "NiveauCentre")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "NiveauCentre.findAll", query = "SELECT n FROM NiveauCentre n")
public class NiveauCentre implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idNiveauCentre;
    @Column(nullable = false, length = 50)
    private String libelleNiveauCentre;
    @Column(nullable = false, length = 1)
    private String temEnServNiveauCentre;
    // bi-directional many-to-one association to CentreGestion
    @OneToMany(mappedBy = "niveauCentre")
    private List<CentreGestion> centreGestions;

    public CentreGestion addCentreGestion(CentreGestion centreGestion) {
        getCentreGestions().add(centreGestion);
        centreGestion.setNiveauCentre(this);
        return centreGestion;
    }

    public CentreGestion removeCentreGestion(CentreGestion centreGestion) {
        getCentreGestions().remove(centreGestion);
        centreGestion.setNiveauCentre(null);
        return centreGestion;
    }
}