package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;

/**
 * The persistent class for the AdminStructure database table.
 *
 */
@Entity
@Table(name = "AdminStructure")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "AdminStructure.findAll", query = "SELECT a FROM AdminStructure a")
public class AdminStructure implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idAdminStructure;
    private Date avantDerniereConnexion;
    @Column(nullable = false)
    private Date dateCreation;
    private Date dateModif;
    private Date derniereConnexion;
    @Column(length = 50)
    private String eppn;
    @Column(length = 50)
    private String login;
    @Column(nullable = false, length = 50)
    private String loginCreation;
    @Column(length = 50)
    private String loginModif;
    @Column(length = 50)
    private String mail;
    @Column(length = 200)
    private String mdp;
    @Column(length = 50)
    private String nom;
    @Column(length = 50)
    private String prenom;
    // bi-directional many-to-one association to Civilite
    @ManyToOne
    @JoinColumn(name = "idCivilite")
    private Civilite civilite;
}