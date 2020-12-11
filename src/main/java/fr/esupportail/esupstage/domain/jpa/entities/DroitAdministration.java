package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the DroitAdministration database table.
 *
 */
@Entity
@Table(name = "DroitAdministration")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "DroitAdministration.findAll", query = "SELECT d FROM DroitAdministration d")
public class DroitAdministration implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idDroitAdministration;
    @Column(nullable = false, length = 50)
    private String libelleDroitAdministration;
    @Column(nullable = false, length = 1)
    private String temEnServDroitAdmin;
    // bi-directional many-to-one association to PersonnelCentreGestion
    @OneToMany(mappedBy = "droitAdministration")
    private List<PersonnelCentreGestion> personnelCentreGestions;

    public PersonnelCentreGestion addPersonnelCentreGestion(PersonnelCentreGestion personnelCentreGestion) {
        getPersonnelCentreGestions().add(personnelCentreGestion);
        personnelCentreGestion.setDroitAdministration(this);
        return personnelCentreGestion;
    }

    public PersonnelCentreGestion removePersonnelCentreGestion(PersonnelCentreGestion personnelCentreGestion) {
        getPersonnelCentreGestions().remove(personnelCentreGestion);
        personnelCentreGestion.setDroitAdministration(null);
        return personnelCentreGestion;
    }
}
