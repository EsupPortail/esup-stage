package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * The persistent class for the Structure database table.
 *
 */
@Entity
@Table(name = "Structure")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "Structure.findAll", query = "SELECT s FROM Structure s")
public class Structure implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idStructure;
    @Lob
    private String activitePrincipale;
    @Column(length = 200)
    private String batimentResidence;
    @Column(length = 10)
    private String codeCommune;
    @Column(length = 20)
    private String codeEtab;
    @Column(length = 10)
    private String codePostal;
    @Column(length = 200)
    private String commune;
    @Column(nullable = false)
    private Date dateCreation;
    private Date dateModif;
    private Date dateStopValidation;
    private Date dateValidation;
    @Column(nullable = false)
    private Integer estValidee;
    @Column(length = 20)
    private String fax;
    @Column(length = 50)
    private String groupe;
    private Date infosAJour;
    @Column(length = 28)
    private String libCedex;
    @Column(nullable = false, length = 50)
    private String loginCreation;
    @Column(length = 50)
    private String loginInfosAJour;
    @Column(length = 50)
    private String loginModif;
    @Column(length = 50)
    private String loginStopValidation;
    @Column(length = 50)
    private String loginValidation;
    @Column(length = 200)
    private String logo;
    @Column(length = 50)
    private String mail;
    @Column(length = 50)
    private String nomDirigeant;
    @Column(length = 14)
    private String numeroSiret;
    @Column(length = 50)
    private String prenomDirigeant;
    @Column(nullable = false, length = 150)
    private String raisonSociale;
    @Column(length = 200)
    private String siteWeb;
    @Column(length = 20)
    private String telephone;
    @Column(length = 1)
    private String temEnServStructure;
    @Column(nullable = false, length = 200)
    private String voie;
    // bi-directional many-to-one association to AccordPartenariat
    @OneToMany(mappedBy = "structure")
    private List<AccordPartenariat> accordPartenariats;
    // bi-directional many-to-one association to Convention
    @OneToMany(mappedBy = "structure")
    private List<Convention> conventions;
    // bi-directional many-to-one association to Offre
    @OneToMany(mappedBy = "structure")
    private List<Offre> offres;
    // bi-directional many-to-one association to Service
    @OneToMany(mappedBy = "structure")
    private List<Service> services;
    // bi-directional many-to-one association to Effectif
    @ManyToOne
    @JoinColumn(name = "idEffectif", nullable = false)
    private Effectif effectif;
    // bi-directional many-to-one association to NafN5
    @ManyToOne
    @JoinColumn(name = "codeNAF_N5")
    private NafN5 nafN5;
    // bi-directional many-to-one association to Pay
    @ManyToOne
    @JoinColumn(name = "idPays", nullable = false)
    private Pays pay;
    // bi-directional many-to-one association to StatutJuridique
    @ManyToOne
    @JoinColumn(name = "idStatutJuridique")
    private StatutJuridique statutJuridique;
    // bi-directional many-to-one association to TypeStructure
    @ManyToOne
    @JoinColumn(name = "idTypeStructure", nullable = false)
    private TypeStructure typeStructure;

    public AccordPartenariat addAccordPartenariat(AccordPartenariat accordPartenariat) {
        getAccordPartenariats().add(accordPartenariat);
        accordPartenariat.setStructure(this);
        return accordPartenariat;
    }

    public AccordPartenariat removeAccordPartenariat(AccordPartenariat accordPartenariat) {
        getAccordPartenariats().remove(accordPartenariat);
        accordPartenariat.setStructure(null);
        return accordPartenariat;
    }

    public Convention addConvention(Convention convention) {
        getConventions().add(convention);
        convention.setStructure(this);
        return convention;
    }

    public Convention removeConvention(Convention convention) {
        getConventions().remove(convention);
        convention.setStructure(null);
        return convention;
    }

    public Offre addOffre(Offre offre) {
        getOffres().add(offre);
        offre.setStructure(this);
        return offre;
    }

    public Offre removeOffre(Offre offre) {
        getOffres().remove(offre);
        offre.setStructure(null);
        return offre;
    }

    public Service addService(Service service) {
        getServices().add(service);
        service.setStructure(this);
        return service;
    }

    public Service removeService(Service service) {
        getServices().remove(service);
        service.setStructure(null);
        return service;
    }

}