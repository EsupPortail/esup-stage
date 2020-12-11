package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the Confidentialite database table.
 *
 */
@Entity
@Table(name = "Confidentialite")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "Confidentialite.findAll", query = "SELECT c FROM Confidentialite c")
public class Confidentialite implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(unique = true, nullable = false, length = 1)
    private String codeConfidentialite;
    @Column(nullable = false, length = 50)
    private String libelleConfidentialite;
    @Column(nullable = false, length = 1)
    private String temEnServConfid;
    // bi-directional many-to-one association to CentreGestion
    @OneToMany(mappedBy = "confidentialite")
    private List<CentreGestion> centreGestions;

    public CentreGestion addCentreGestion(CentreGestion centreGestion) {
        getCentreGestions().add(centreGestion);
        centreGestion.setConfidentialite(this);
        return centreGestion;
    }

    public CentreGestion removeCentreGestion(CentreGestion centreGestion) {
        getCentreGestions().remove(centreGestion);
        centreGestion.setConfidentialite(null);
        return centreGestion;
    }
}