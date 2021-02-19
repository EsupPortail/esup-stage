package fr.esupportail.esupstage.domain.jpa.entities;


import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the FAP_N2 database table.
 *
 */
@Entity
@Table(name = "FAP_N2")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "FapN2.findAll", query = "SELECT f FROM FapN2 f")
public class FapN2 implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(unique = true, nullable = false, length = 3)
    private String codeFAP_N2;
    @Column(nullable = false, length = 200)
    private String libelle;
    // bi-directional many-to-one association to FapN1
    @ManyToOne
    @JoinColumn(name = "codeFAP_N1", nullable = false)
    private FapN1 fapN1;
    // bi-directional many-to-one association to FapN3
    @OneToMany(mappedBy = "fapN2")
    private List<FapN3> fapN3s;

    public FapN3 addFapN3(FapN3 fapN3) {
        getFapN3s().add(fapN3);
        fapN3.setFapN2(this);
        return fapN3;
    }

    public FapN3 removeFapN3(FapN3 fapN3) {
        getFapN3s().remove(fapN3);
        fapN3.setFapN2(null);
        return fapN3;
    }
}