package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PersonnelCentreGestion")
@Data
public class PersonnelCentreGestion extends ObjetMetier implements Exportable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPersonnelCentreGestion", nullable = false)

    private int id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column
    private String mail;

    @Column(name = "telephone")
    private String tel;

    @Column
    private String fax;

    @ManyToOne
    @JoinColumn(name = "idCivilite")
    private Civilite civilite;

    @Column
    private String codeUniversite;

    @Column
    private String typePersonne;

    @ManyToOne
    @JoinColumn(name = "idCentreGestion", nullable = false)
    @JsonBackReference
    private CentreGestion centreGestion;

    @Column(nullable = false)
    private String uidPersonnel;

    @Column
    private String fonction;

    @Column(nullable = false)
    private boolean impressionConvention;

    @Column
    private String campus;

    @Column
    private String batiment;

    @Column
    private String bureau;

    @Column(nullable = false)
    private String codeAffectation;

    @Column(nullable = false)
    private String codeUniversiteAffectation;

    private Boolean alertesMail;
    private Boolean droitEvaluationEtudiant;
    private Boolean droitEvaluationEnseignant;
    private Boolean droitEvaluationEntreprise;
    private Boolean creationConventionEtudiant;
    private Boolean modificationConventionEtudiant;
    private Boolean creationConventionGestionnaire;
    private Boolean modificationConventionGestionnaire;
    private Boolean creationAvenantEtudiant;
    private Boolean modificationAvenantEtudiant;
    private Boolean creationAvenantGestionnaire;
    private Boolean modificationAvenantGestionnaire;
    private Boolean validationPedagogiqueConvention;
    private Boolean validationAdministrativeConvention;
    private Boolean verificationAdministrativeConvention;
    private Boolean validationAvenant;
    private Boolean ConventionSignee;

    @Override
    public String getExportValue(String key) {
        String value = "";
        switch (key) {
            case "civilite":
                if (getCivilite() != null) {
                    value = getCivilite().getLibelle();
                }
                break;
            case "nom":
                value = getNom();
                break;
            case "prenom":
                value = getPrenom();
                break;
            case "alertesMail":
                value = getAlertesMail() != null && getAlertesMail() ? "Oui" : "Non";
                break;
            case "droitsEvaluation":
                List<String> droits = new ArrayList<>();
                if (getDroitEvaluationEtudiant() != null && getDroitEvaluationEtudiant()) droits.add("Étudiant");
                if (getDroitEvaluationEnseignant() != null && getDroitEvaluationEnseignant()) droits.add("Enseignant");
                if (getDroitEvaluationEntreprise() != null && getDroitEvaluationEntreprise()) droits.add("Entreprise");
                value = String.join(", ", droits);
                break;
            default:
                break;
        }
        return value;
    }
}
