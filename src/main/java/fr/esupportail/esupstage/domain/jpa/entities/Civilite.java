package fr.esupportail.esupstage.domain.jpa.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the Civilite database table.
 *
 */
@Entity
@Table(name = "Civilite")
@NamedQuery(name = "Civilite.findAll", query = "SELECT c FROM Civilite c")
@Getter
@Setter
@NoArgsConstructor
public class Civilite implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idCivilite;
    @Column(nullable = false, length = 50)
    private String libelleCivilite;
    // bi-directional many-to-one association to AdminStructure
    @OneToMany(mappedBy = "civilite")
    private List<AdminStructure> adminStructures;
    // bi-directional many-to-one association to Contact
    @OneToMany(mappedBy = "civilite")
    private List<Contact> contacts;
    // bi-directional many-to-one association to PersonnelCentreGestion
    @OneToMany(mappedBy = "civilite")
    private List<PersonnelCentreGestion> personnelCentreGestions;

    public AdminStructure addAdminStructure(AdminStructure adminStructure) {
        getAdminStructures().add(adminStructure);
        adminStructure.setCivilite(this);
        return adminStructure;
    }

    public AdminStructure removeAdminStructure(AdminStructure adminStructure) {
        getAdminStructures().remove(adminStructure);
        adminStructure.setCivilite(null);
        return adminStructure;
    }

    public Contact addContact(Contact contact) {
        getContacts().add(contact);
        contact.setCivilite(this);
        return contact;
    }

    public Contact removeContact(Contact contact) {
        getContacts().remove(contact);
        contact.setCivilite(null);
        return contact;
    }

    public PersonnelCentreGestion addPersonnelCentreGestion(PersonnelCentreGestion personnelCentreGestion) {
        getPersonnelCentreGestions().add(personnelCentreGestion);
        personnelCentreGestion.setCivilite(this);
        return personnelCentreGestion;
    }

    public PersonnelCentreGestion removePersonnelCentreGestion(PersonnelCentreGestion personnelCentreGestion) {
        getPersonnelCentreGestions().remove(personnelCentreGestion);
        personnelCentreGestion.setCivilite(null);
        return personnelCentreGestion;
    }
}