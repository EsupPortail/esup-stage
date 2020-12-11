package fr.esupportail.esupstage.domain.jpa.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the FAP_N3 database table.
 *
 */
@Entity
@Table(name = "FAP_N3")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "FapN3.findAll", query = "SELECT f FROM FapN3 f")
public class FapN3 implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(unique = true, nullable = false, length = 5)
    private String codeFAP_N3;
    @Column(nullable = false, length = 200)
    private String libelle;
    // bi-directional many-to-one association to FapN2
    @ManyToOne
    @JoinColumn(name = "codeFAP_N2", nullable = false)
    private FapN2 fapN2;
    // bi-directional many-to-one association to FAP_Qualification
    @ManyToOne
    @JoinColumn(name = "numFAP_Qualification", nullable = false)
    private FAP_Qualification fapQualification;

}