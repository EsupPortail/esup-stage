package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the CaisseRegime database table.
 *
 */
@Entity
@Table(name = "CaisseRegime")
@NamedQuery(name = "CaisseRegime.findAll", query = "SELECT c FROM CaisseRegime c")
@Setter
@Getter
@NoArgsConstructor
public class CaisseRegime implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(unique = true, nullable = false, length = 5)
    private String codeCaisse;
    @Column(nullable = false, length = 100)
    private String infoCaisse;
    @Column(nullable = false, length = 100)
    private String libelleCaisse;
    private boolean modifiable;
    @Column(nullable = false, length = 1)
    private String temEnServCaisse;

}