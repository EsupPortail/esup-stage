package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import org.esup_portail.esup_stage.dto.view.Views;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Avenant")
@Data
public class Avenant extends ObjetMetier implements Exportable {

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAvenant", nullable = false)
    private int id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "idConvention", nullable = false)
    private Convention convention;

    @JsonView(Views.List.class)
    @Lob
    private String titreAvenant;

    @Lob
    private String motifAvenant;

    @Column(nullable = false)
    private boolean rupture;

    @Column(nullable = false)
    private boolean modificationPeriode;

    @Temporal(TemporalType.DATE)
    private Date dateDebutStage;

    @Temporal(TemporalType.DATE)
    private Date dateFinStage;

    @Column(nullable = false)
    private boolean interruptionStage;

    @Temporal(TemporalType.DATE)
    private Date dateDebutInterruption;

    @Temporal(TemporalType.DATE)
    private Date dateFinInterruption;

    private Boolean modificationLieu;

    @ManyToOne
    @JoinColumn(name = "idService")
    private Service service;

    @Column(nullable = false)
    private boolean modificationSujet;

    @Lob
    private String sujetStage;

    @Column(nullable = false)
    private boolean modificationEnseignant;

    @Column(nullable = false)
    private boolean modificationSalarie;

    @ManyToOne
    @JoinColumn(name = "idContact")
    private Contact contact;

    @Column(nullable = false)
    private boolean validationAvenant;

    @ManyToOne
    @JoinColumn(name = "idEnseignant")
    private Enseignant enseignant;

    @Column
    private String montantGratification;

    @ManyToOne
    @JoinColumn(name = "idUniteGratification")
    private UniteGratification uniteGratification;

    @ManyToOne
    @JoinColumn(name = "idModeVersGratification")
    private ModeVersGratification modeVersGratification;

    @ManyToOne
    @JoinColumn(name = "idDevise")
    private Devise devise;

    @Column(nullable = false)
    private boolean modificationMontantGratification;

    @Temporal(TemporalType.DATE)
    private Date dateRupture;

    @Lob
    private String commentaireRupture;

    @Column
    private String monnaieGratification;

    @ManyToOne
    @JoinColumn(name = "idUniteDureeGratification")
    private UniteDuree uniteDuree;

    @JsonIgnore
    @OneToMany(mappedBy = "avenant")
    private List<PeriodeInterruptionAvenant> periodeInterruptionAvenants = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateValidation;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnvoiSignature;

    @Column(length = 255)
    private String documentId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSignatureEtudiant;

    @Column
    private Date dateDepotEtudiant;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSignatureEnseignant;

    @Column
    private Date dateDepotEnseignant;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSignatureTuteur;

    @Column
    private Date dateDepotTuteur;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSignatureSignataire;

    @Column
    private Date dateDepotSignataire;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSignatureViseur;

    @Column
    private Date dateDepotViseur;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateActualisationSignature;

    @Column
    private String loginEnvoiSignature;

    @Column
    private boolean temAvenantSigne;

    public List<PeriodeInterruptionAvenant> getPeriodeInterruptionAvenants() {
        if (periodeInterruptionAvenants != null) {
            // Ordonne par ordre de début asc
            periodeInterruptionAvenants.sort(Comparator.comparing(PeriodeInterruptionAvenant::getDateDebutInterruption));
        }
        return periodeInterruptionAvenants;
    }

    @Transient
    public boolean isAllSignedDateSetted() {
        return getDateDepotEtudiant() != null && getDateSignatureEtudiant() != null &&
                getDateDepotEnseignant() != null && getDateSignatureEnseignant() != null &&
                getDateDepotTuteur() != null && getDateSignatureTuteur() != null &&
                getDateDepotSignataire() != null && getDateSignatureSignataire() != null &&
                getDateDepotViseur() != null && getDateSignatureViseur() != null;
    }

    @Override
    public String getExportValue(String key) {
        return "";
    }
}
