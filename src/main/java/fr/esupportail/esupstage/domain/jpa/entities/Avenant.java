package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;

/**
 * The persistent class for the Avenant database table.
 *
 */
@Entity
@Table(name = "Avenant")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "Avenant.findAll", query = "SELECT a FROM Avenant a")
public class Avenant implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer idAvenant;
    @Lob
    private String commentaireRupture;
    @Column(nullable = false)
    private Date dateCreation;
    private Date dateDebutInterruption;
    private Date dateDebutStage;
    private Date dateFinInterruption;
    private Date dateFinStage;
    private Date dateModif;
    private Date dateRupture;
    private Integer idEnseignant;
    private Integer idUniteDureeGratification;
    private Integer idUniteGratification;
    @Column(nullable = false)
    private boolean interruptionStage;
    @Column(nullable = false, length = 50)
    private String loginCreation;
    @Column(length = 50)
    private String loginModif;
    @Column(nullable = false)
    private boolean modificationEnseignant;
    private boolean modificationLieu;
    @Column(nullable = false)
    private boolean modificationMontantGratification;
    @Column(nullable = false)
    private boolean modificationPeriode;
    @Column(nullable = false)
    private boolean modificationSalarie;
    @Column(nullable = false)
    private boolean modificationSujet;
    // @Column(length = 50)
    // private String monnaieGratification;
    @Column(length = 7)
    private String montantGratification;
    @Lob
    private String motifAvenant;
    @Column(nullable = false)
    private boolean rupture;
    @Lob
    private String sujetStage;
    @Lob
    private String titreAvenant;
    @Column(nullable = false)
    private boolean validationAvenant;
    // bi-directional many-to-one association to Contact
    @ManyToOne
    @JoinColumn(name = "idContact")
    private Contact contact;
    // bi-directional many-to-one association to Convention
    @ManyToOne
    @JoinColumn(name = "idConvention", nullable = false)
    private Convention convention;
    // bi-directional many-to-one association to Service
    @ManyToOne
    @JoinColumn(name = "idService")
    private Service service;

}