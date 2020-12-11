package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the Pays database table.
 *
 */
@Entity
@Table(name = "Pays")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "Pay.findAll", query = "SELECT p FROM Pays p")
public class Pays implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idPays;
    @Column(nullable = false)
    private Integer actual;
    @Column(name = "COG", nullable = false)
    private Integer cog;
    @Column(name = "CRPAY")
    private Integer crpay;
    @Column(name = "ISO2", length = 2)
    private String iso2;
    @Column(nullable = false, length = 70)
    private String lib;
    @Column(nullable = false)
    private boolean siretObligatoire;
    @Column(nullable = false, length = 1)
    private String temEnServPays;
    // bi-directional many-to-one association to Offre
    @OneToMany(mappedBy = "pay")
    private List<Offre> offres;
    // bi-directional many-to-one association to Service
    @OneToMany(mappedBy = "pay")
    private List<Service> services;
    // bi-directional many-to-one association to Structure
    @OneToMany(mappedBy = "pay")
    private List<Structure> structures;


    public Offre addOffre(Offre offre) {
        getOffres().add(offre);
        offre.setPay(this);
        return offre;
    }

    public Offre removeOffre(Offre offre) {
        getOffres().remove(offre);
        offre.setPay(null);
        return offre;
    }

    public Service addService(Service service) {
        getServices().add(service);
        service.setPay(this);
        return service;
    }

    public Service removeService(Service service) {
        getServices().remove(service);
        service.setPay(null);
        return service;
    }

    public Structure addStructure(Structure structure) {
        getStructures().add(structure);
        structure.setPay(this);
        return structure;
    }

    public Structure removeStructure(Structure structure) {
        getStructures().remove(structure);
        structure.setPay(null);
        return structure;
    }
}