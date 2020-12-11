package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * The persistent class for the PersonnelCentreGestion database table.
 *
 */
@Entity
@Table(name = "PersonnelCentreGestion")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "PersonnelCentreGestion.findAll", query = "SELECT p FROM PersonnelCentreGestion p")
public class PersonnelCentreGestion implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idPersonnelCentreGestion;
    private boolean alertesMail;
    @Column(length = 50)
    private String batiment;
    @Column(length = 50)
    private String bureau;
    @Column(length = 250)
    private String campus;
    @Column(length = 50)
    private String codeUniversite;
    @Column(nullable = false)
    private Date dateCreation;
    private Date dateModif;
    private boolean droitEvaluationEnseignant;
    private boolean droitEvaluationEntreprise;
    private boolean droitEvaluationEtudiant;
    @Column(length = 50)
    private String fax;
    @Column(length = 50)
    private String fonction;
    @Column(nullable = false)
    private boolean impressionConvention;
    @Column(nullable = false, length = 50)
    private String loginCreation;
    @Column(length = 50)
    private String loginModif;
    @Column(length = 50)
    private String mail;
    @Column(nullable = false, length = 50)
    private String nom;
    @Column(nullable = false, length = 50)
    private String prenom;
    @Column(length = 50)
    private String telephone;
    @Column(length = 50)
    private String typePersonne;
    @Column(nullable = false, length = 50)
    private String uidPersonnel;
    // bi-directional many-to-one association to Offre
    @OneToMany(mappedBy = "personnelCentreGestion")
    private List<Offre> offres;
    // bi-directional many-to-one association to Affectation
    @ManyToOne
    @JoinColumns({ @JoinColumn(name = "codeAffectation", referencedColumnName = "codeAffectation", nullable = false),
            @JoinColumn(name = "codeUniversiteAffectation", referencedColumnName = "codeUniversite", nullable = false) })
    private Affectation affectation;
    // bi-directional many-to-one association to CentreGestion
    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    private CentreGestion centreGestion;
    // bi-directional many-to-one association to Civilite
    @ManyToOne
    @JoinColumn(name = "idCivilite")
    private Civilite civilite;
    // bi-directional many-to-one association to DroitAdministration
    @ManyToOne
    @JoinColumn(name = "idDroitAdministration", nullable = false)
    private DroitAdministration droitAdministration;

    public Offre addOffre(Offre offre) {
        getOffres().add(offre);
        offre.setPersonnelCentreGestion(this);
        return offre;
    }

    public Offre removeOffre(Offre offre) {
        getOffres().remove(offre);
        offre.setPersonnelCentreGestion(null);
        return offre;
    }


}