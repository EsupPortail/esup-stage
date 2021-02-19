package fr.esupportail.esupstage.domain.jpa.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class Contact extends Auditable<String> {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idContact;
    private LocalDateTime avantDerniereConnexion;
    @Lob
    private String commentaire;
    private LocalDateTime derniereConnexion;
    @Column(length = 50)
    private String fax;
    @Column(nullable = false, length = 100)
    private String fonction;
    private LocalDate infosAJour;
    @Column(length = 12)
    private String login;
    @Column(length = 50)
    private String loginInfosAJour;
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