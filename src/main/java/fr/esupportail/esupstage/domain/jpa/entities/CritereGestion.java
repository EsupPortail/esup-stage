package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the CritereGestion database table.
 *
 */
@Entity
@Table(name = "CritereGestion")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "CritereGestion.findAll", query = "SELECT c FROM CritereGestion c")
public class CritereGestion implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    private CritereGestionPK id;
    @Column(nullable = false, length = 200)
    private String libelleCritere;
    // bi-directional many-to-one association to CentreGestion
    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    private CentreGestion centreGestion;

}