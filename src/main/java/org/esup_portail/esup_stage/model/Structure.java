package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import org.esup_portail.esup_stage.dto.view.Views;

import java.util.Date;

@Entity
@Table(name = "Structure")
@Data
public class Structure extends ObjetMetier implements Exportable {

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idStructure", nullable = false)
    private Integer id;

    @Column
    private String libCedex;

    @Column
    private String codeEtab;

    @JsonView(Views.List.class)
    @Column
    private String numeroSiret;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumn(name = "codeNAF_N5")
    private NafN5 nafN5;

    @JsonView(Views.List.class)
    @Column(nullable = false)
    private String raisonSociale;

    @Lob
    private String activitePrincipale;

    @Column
    private String telephone;

    @Column
    private String fax;

    @Column
    private String mail;

    @Column
    private String siteWeb;

    @Column
    private String groupe;

    @Column
    private String logo;

    @Column(nullable = false)
    private boolean estValidee;

    @Column
    private String loginValidation;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateValidation;

    @Column
    private String loginStopValidation;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateStopValidation;

    @Temporal(TemporalType.DATE)
    private Date infosAJour;

    @Column
    private String loginInfosAJour;

    @ManyToOne
    @JoinColumn(name = "idEffectif", nullable = false)
    private Effectif effectif;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumn(name = "idStatutJuridique")
    private StatutJuridique statutJuridique;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumn(name = "idTypeStructure", nullable = false)
    private TypeStructure typeStructure;

    @Column
    private String nomDirigeant;

    @Column
    private String prenomDirigeant;

    @Column(columnDefinition = "boolean default true")
    private Boolean temEnServStructure;

    @Column
    private String batimentResidence;

    @Column(nullable = false)
    private String voie;

    @JsonView(Views.List.class)
    @Column
    private String commune;

    @Column
    private String codePostal;

    @Column
    private String codeCommune;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumn(name = "idPays", nullable = false)
    private Pays pays;

    @Column(length = 8)
    private String numeroRNE;

    @Column
    private boolean temSiren;

    public Structure() {
    }

    

    @Override
    public String getExportValue(String key) {
        String value = "";
        switch (key) {
            case "raisonSociale":
                value = getRaisonSociale();
                break;
            case "numeroSiret":
                value = getNumeroSiret();
                break;
            case "nafN5":
                if (getNafN5() != null) {
                    value = getNafN5().getLibelle();
                }
                break;
            case "pays":
                if (getPays() != null) {
                    value = getPays().getLib();
                }
                break;
            case "commune":
                value = getCommune();
                break;
            case "typeStructure":
                if (getTypeStructure() != null) {
                    value = getTypeStructure().getLibelle();
                }
                break;
            case "statutJuridique":
                if (getStatutJuridique() != null) {
                    value = getStatutJuridique().getLibelle();
                }
                break;
            default:
                break;
        }
        return value;
    }
}
