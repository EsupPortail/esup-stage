package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

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
public class AdminStructure extends Auditable<String> {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idAdminStructure;
    private LocalDateTime avantDerniereConnexion;
    private LocalDateTime derniereConnexion;
    @Column(length = 50)
    private String eppn;
    @Column(length = 50)
    private String login;
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