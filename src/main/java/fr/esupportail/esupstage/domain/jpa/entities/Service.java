package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * The persistent class for the Service database table.
 *
 */
@Entity
@Table(name = "Service")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "Service.findAll", query = "SELECT s FROM Service s")
public class Service implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idService;
    @Column(length = 200)
    private String batimentResidence;
    @Column(length = 10)
    private String codeCommune;
    @Column(nullable = false, length = 10)
    private String codePostal;
    @Column(length = 200)
    private String commune;
    @Column(nullable = false)
    private Date dateCreation;
    private Date dateModif;
    private Date infosAJour;
    @Column(nullable = false, length = 50)
    private String loginCreation;
    @Column(length = 50)
    private String loginInfosAJour;
    @Column(length = 50)
    private String loginModif;
    @Column(nullable = false, length = 70)
    private String nom;
    @Column(length = 20)
    private String telephone;
    @Column(nullable = false, length = 200)
    private String voie;
    // bi-directional many-to-one association to Avenant
    @OneToMany(mappedBy = "service")
    private List<Avenant> avenants;
    // bi-directional many-to-one association to Contact
    @OneToMany(mappedBy = "service")
    private List<Contact> contacts;
    // bi-directional many-to-one association to Convention
    @OneToMany(mappedBy = "service")
    private List<Convention> conventions;
    // bi-directional many-to-one association to Pay
    @ManyToOne
    @JoinColumn(name = "idPays", nullable = false)
    private Pays pay;
    // bi-directional many-to-one association to Structure
    @ManyToOne
    @JoinColumn(name = "idStructure", nullable = false)
    private Structure structure;


    public Avenant addAvenant(Avenant avenant) {
        getAvenants().add(avenant);
        avenant.setService(this);
        return avenant;
    }

    public Avenant removeAvenant(Avenant avenant) {
        getAvenants().remove(avenant);
        avenant.setService(null);
        return avenant;
    }


    public Contact addContact(Contact contact) {
        getContacts().add(contact);
        contact.setService(this);
        return contact;
    }

    public Contact removeContact(Contact contact) {
        getContacts().remove(contact);
        contact.setService(null);
        return contact;
    }

    public Convention addConvention(Convention convention) {
        getConventions().add(convention);
        convention.setService(this);
        return convention;
    }

    public Convention removeConvention(Convention convention) {
        getConventions().remove(convention);
        convention.setService(null);
        return convention;
    }
}