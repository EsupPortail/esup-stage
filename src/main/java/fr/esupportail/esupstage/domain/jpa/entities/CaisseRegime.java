package fr.esupportail.esupstage.domain.jpa.entities;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Column(name = "libelleCaisse", nullable = false, length = 100)
    private String label;
    private boolean modifiable;
    @Column(name = "temEnServCaisse", nullable = false, length = 1)
    private String temEnServ;

}