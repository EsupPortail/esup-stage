package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the Affectation database table.
 *
 */
@Entity
@Table(name = "Affectation")
@NamedQuery(name = "Affectation.findAll", query = "SELECT a FROM Affectation a")
@Getter
@Setter
@NoArgsConstructor
public class Affectation implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    private AffectationPK id;
    @Column(length = 150)
    private String libelleAffectation;
    // bi-directional many-to-one association to Enseignant
    @OneToMany(mappedBy = "affectation")
    private List<Enseignant> enseignants;
    // bi-directional many-to-one association to PersonnelCentreGestion
    @OneToMany(mappedBy = "affectation")
    private List<PersonnelCentreGestion> personnelCentreGestions;

    public Enseignant addEnseignant(Enseignant enseignant) {
        getEnseignants().add(enseignant);
        enseignant.setAffectation(this);
        return enseignant;
    }

    public Enseignant removeEnseignant(Enseignant enseignant) {
        getEnseignants().remove(enseignant);
        enseignant.setAffectation(null);
        return enseignant;
    }

    public List<PersonnelCentreGestion> getPersonnelCentreGestions() {
        return this.personnelCentreGestions;
    }

    public void setPersonnelCentreGestions(List<PersonnelCentreGestion> personnelCentreGestions) {
        this.personnelCentreGestions = personnelCentreGestions;
    }

    public PersonnelCentreGestion addPersonnelCentreGestion(PersonnelCentreGestion personnelCentreGestion) {
        getPersonnelCentreGestions().add(personnelCentreGestion);
        personnelCentreGestion.setAffectation(this);
        return personnelCentreGestion;
    }

    public PersonnelCentreGestion removePersonnelCentreGestion(PersonnelCentreGestion personnelCentreGestion) {
        getPersonnelCentreGestions().remove(personnelCentreGestion);
        personnelCentreGestion.setAffectation(null);
        return personnelCentreGestion;
    }
}