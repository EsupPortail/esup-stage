package fr.esupportail.esupstage.domain.jpa.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the CentreGestionSuperViseur database table.
 *
 */
@Entity
@Table(name = "CentreGestionSuperViseur")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "CentreGestionSuperViseur.findAll", query = "SELECT c FROM CentreGestionSuperViseur c")
public class CentreGestionSuperViseur implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idCentreGestionSuperViseur;
    @Column(nullable = false, length = 100)
    private String nomCentreSuperViseur;
    // bi-directional many-to-one association to CentreGestion
    @OneToMany(mappedBy = "centreGestionSuperViseur")
    private List<CentreGestion> centreGestions;

    public CentreGestion addCentreGestion(CentreGestion centreGestion) {
        getCentreGestions().add(centreGestion);
        centreGestion.setCentreGestionSuperViseur(this);
        return centreGestion;
    }

    public CentreGestion removeCentreGestion(CentreGestion centreGestion) {
        getCentreGestions().remove(centreGestion);
        centreGestion.setCentreGestionSuperViseur(null);
        return centreGestion;
    }
}