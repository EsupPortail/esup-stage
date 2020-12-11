package fr.esupportail.esupstage.domain.jpa.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * The persistent class for the Etudiant database table.
 *
 */
@Entity
@Table(name = "Etudiant")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "Etudiant.findAll", query = "SELECT e FROM Etudiant e")
public class Etudiant implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idEtudiant;
    @Column(length = 1)
    private String codeSexe;
    @Column(nullable = false, length = 50)
    private String codeUniversite;
    @Column(nullable = false)
    private Date dateCreation;
    private Date dateModif;
    private Date dateNais;
    @Column(nullable = false, length = 50)
    private String identEtudiant;
    @Column(nullable = false, length = 50)
    private String loginCreation;
    @Column(length = 50)
    private String loginModif;
    @Column(length = 100)
    private String mail;
    @Column(nullable = false, length = 50)
    private String nom;
    @Column(length = 50)
    private String nomMarital;
    @Column(nullable = false, length = 20)
    private String numEtudiant;
    @Column(length = 15)
    private String numSS;
    @Column(nullable = false, length = 50)
    private String prenom;
    // bi-directional many-to-one association to Convention
    @OneToMany(mappedBy = "etudiant")
    private List<Convention> conventions;

    public Convention addConvention(Convention convention) {
        getConventions().add(convention);
        convention.setEtudiant(this);
        return convention;
    }

    public Convention removeConvention(Convention convention) {
        getConventions().remove(convention);
        convention.setEtudiant(null);
        return convention;
    }
}