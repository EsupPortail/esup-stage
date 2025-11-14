package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.esup_portail.esup_stage.dto.view.Views;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "CentreGestion")
public class CentreGestion extends ObjetMetier implements Exportable {

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCentreGestion", nullable = false)
    private int id;

    @JsonView(Views.List.class)
    @Column
    private String nomCentre;

    @ManyToOne
    @JoinColumn(name = "idNiveauCentre", nullable = false)
    private NiveauCentre niveauCentre;

    @Column
    private String siteWeb;

    @Column
    private String mail;

    @Column
    private String telephone;

    @Column
    private String fax;

    @Lob
    private String commentaire;

    @Column()
    private boolean presenceTuteurEns;

    @Column()
    private boolean presenceTuteurPro;

    @Column()
    private boolean saisieTuteurProParEtudiant;

    @Column
    private boolean depotAnonyme;

    @Column(nullable = false)
    private String codeUniversite;

    @Column
    private String nomViseur;

    @Column
    private String prenomViseur;

    @Column
    private String qualiteViseur;

    @Column
    private String mailViseur;

    @Column
    private String urlPageInstruction;

    @ManyToOne
    @JoinColumn(name = "idCentreGestionSuperViseur")
    private CentreGestionSuperviseur centreGestionSuperViseur;

    @ManyToOne
    @JoinColumn(name = "codeConfidentialite")
    private Confidentialite codeConfidentialite;

    @Column()
    private boolean autoriserImpressionConvention;

    @Column()
    private Integer conditionValidationImpression = 0;

    @ManyToOne
    @JoinColumn(name = "idFichier")
    private Fichier fichier;

    @JsonView(Views.List.class)
    @Column()
    private Boolean validationPedagogique = false;

    @JsonView(Views.List.class)
    @Column()
    private Boolean validationConvention = false;

    @JsonView(Views.List.class)
    @Column()
    private Boolean verificationAdministrative = false;

    @JsonView(Views.List.class)
    private Integer validationPedagogiqueOrdre;

    @JsonView(Views.List.class)
    private Integer validationConventionOrdre;

    @JsonView(Views.List.class)
    private Integer verificationAdministrativeOrdre;

    @Column()
    private boolean autorisationEtudiantCreationConvention;

    @ManyToOne
    @JoinColumn(name = "idModeValidationStage")
    private ModeValidationStage modeValidationStage;

    @Column()
    private Boolean visibiliteEvalPro = false;

    @Column()
    private Boolean recupInscriptionAnterieure = false;

    private Integer dureeRecupInscriptionAnterieure = 0;

    @Column
    private String adresse;

    @Column
    private String voie;

    @Column
    private String commune;

    @Column
    private String codePostal;

    @OneToMany(mappedBy = "centreGestion", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private List<PersonnelCentreGestion> personnels = new ArrayList<>();

    @Column(nullable = false)
    private boolean validationCreation = false;

    @JsonIgnore
    @OneToMany(mappedBy = "centreGestion", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CritereGestion> criteres = new ArrayList<>();

    @Column()
    private Integer delaiAlerteConvention = 0;

    @OneToOne(mappedBy = "centreGestion", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Consigne consigne;

    @JsonView(Views.List.class)
    @OneToOne(mappedBy = "centreGestion", cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private FicheEvaluation ficheEvaluation;

    @Column(length = 255)
    private String circuitSignature;

    @OneToMany(mappedBy = "centreGestion", cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.PERSIST})
    @OrderBy("ordre ASC")
    @JsonManagedReference
    private List<CentreGestionSignataire> signataires = new ArrayList<>();

    @JsonView(Views.List.class)
    @Column()
    private Boolean onlyMailCentreGestion = false;

    @Column
    private boolean envoiDocumentSigne = false;

    @JsonView(Views.List.class)
    @Column()
    private boolean autoriserChevauchement = false;

    @JsonView(Views.List.class)
    @Column()
    private boolean autoriserImpressionConventionApresCreationAvenant = false;

    @Column
    private String nomDelegataireViseur;

    @Column
    private String prenomDelegataireViseur;

    @Column
    private String qualiteDelegataireViseur;

    @Column
    private String mailDelegataireViseur;

    @Transient
    public String getAdresseComplete() {
        return getVoie() + " " + getCodePostal() + " " + getCommune();
    }

    public boolean isOnlyMailCentreGestion() {
        return onlyMailCentreGestion;
    }

    @Override
    public String getExportValue(String key) {
        String value = "";
        switch (key) {
            case "id":
                value = String.valueOf(getId());
                break;
            case "nomCentre":
                value = getNomCentre();
                break;
            case "niveauCentre":
                if (getNiveauCentre() != null) {
                    value = getNiveauCentre().getLibelle();
                }
                break;
            case "validationPedagogique":
                value = getValidationPedagogique() != null && getValidationPedagogique() ? "Oui" : "Non";
                break;
            case "codeConfidentialite":
                if (getCodeConfidentialite() != null) {
                    value = getCodeConfidentialite().getLibelle();
                }
                break;
            default:
                break;
        }
        return value;
    }
}
