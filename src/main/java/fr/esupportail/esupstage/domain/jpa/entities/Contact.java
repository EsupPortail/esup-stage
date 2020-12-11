package fr.esupportail.esupstage.domain.jpa.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * The persistent class for the Contact database table.
 *
 */
@Entity
@Table(name = "Contact")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "Contact.findAll", query = "SELECT c FROM Contact c")
public class Contact implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idContact;
    private Date avantDerniereConnexion;
    @Lob
    private String commentaire;
    @Column(nullable = false)
    private Date dateCreation;
    private Date dateModif;
    private Date derniereConnexion;
    @Column(length = 50)
    private String fax;
    @Column(nullable = false, length = 100)
    private String fonction;
    private Date infosAJour;
    @Column(length = 12)
    private String login;
    @Column(nullable = false, length = 50)
    private String loginCreation;
    @Column(length = 50)
    private String loginInfosAJour;
    @Column(length = 50)
    private String loginModif;
    @Column(length = 50)
    private String mail;
    @Column(length = 200)
    private String mdp;
    @Column(nullable = false, length = 50)
    private String nom;
    @Column(nullable = false, length = 50)
    private String prenom;
    @Column(length = 50)
    private String tel;
    // bi-directional many-to-one association to AccordPartenariat
    @OneToMany(mappedBy = "contact")
    private List<AccordPartenariat> accordPartenariats;
    // bi-directional many-to-one association to Avenant
    @OneToMany(mappedBy = "contact")
    private List<Avenant> avenants;
    // bi-directional many-to-one association to CentreGestion
    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    private CentreGestion centreGestion;
    // bi-directional many-to-one association to Civilite
    @ManyToOne
    @JoinColumn(name = "idCivilite")
    private Civilite civilite;
    // bi-directional many-to-one association to Service
    @ManyToOne
    @JoinColumn(name = "idService", nullable = false)
    private Service service;
    // bi-directional many-to-one association to Convention
    @OneToMany(mappedBy = "contact1")
    private List<Convention> conventions1;
    // bi-directional many-to-one association to Convention
    @OneToMany(mappedBy = "contact2")
    private List<Convention> conventions2;
    // bi-directional many-to-one association to Offre
    @OneToMany(mappedBy = "contact1")
    private List<Offre> offres1;
    // bi-directional many-to-one association to Offre
    @OneToMany(mappedBy = "contact2")
    private List<Offre> offres2;
    // bi-directional many-to-one association to Offre
    @OneToMany(mappedBy = "contact3")
    private List<Offre> offres3;

    public AccordPartenariat addAccordPartenariat(AccordPartenariat accordPartenariat) {
        getAccordPartenariats().add(accordPartenariat);
        accordPartenariat.setContact(this);
        return accordPartenariat;
    }

    public AccordPartenariat removeAccordPartenariat(AccordPartenariat accordPartenariat) {
        getAccordPartenariats().remove(accordPartenariat);
        accordPartenariat.setContact(null);
        return accordPartenariat;
    }

    public Avenant addAvenant(Avenant avenant) {
        getAvenants().add(avenant);
        avenant.setContact(this);
        return avenant;
    }

    public Avenant removeAvenant(Avenant avenant) {
        getAvenants().remove(avenant);
        avenant.setContact(null);
        return avenant;
    }

    public Convention addConventions1(Convention conventions1) {
        getConventions1().add(conventions1);
        conventions1.setContact1(this);
        return conventions1;
    }

    public Convention removeConventions1(Convention conventions1) {
        getConventions1().remove(conventions1);
        conventions1.setContact1(null);
        return conventions1;
    }

    public Convention addConventions(Convention conventions2) {
        getConventions2().add(conventions2);
        conventions2.setContact2(this);
        return conventions2;
    }

    public Convention removeConventions2(Convention conventions2) {
        getConventions2().remove(conventions2);
        conventions2.setContact2(null);
        return conventions2;
    }

}