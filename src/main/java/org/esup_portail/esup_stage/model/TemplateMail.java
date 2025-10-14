package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;
import lombok.Data;
import org.esup_portail.esup_stage.dto.TemplateMailInterface;

@Entity
@Data
@Table(name = "TemplateMail")
public class TemplateMail extends ObjetMetier implements TemplateMailInterface, Exportable {

    public static final String CODE_AVENANT_VALIDATION = "AVENANT_VALIDATION";
    public static final String CODE_CONVENTION_VALID_ADMINISTRATIVE = "CONVENTION_VALID_ADMINISTRATIVE";
    public static final String CODE_CONVENTION_DEVALID_ADMINISTRATIVE = "CONVENTION_DEVALID_ADMINISTRATIVE";
    public static final String CODE_CONVENTION_VALID_PEDAGOGIQUE = "CONVENTION_VALID_PEDAGOGIQUE";
    public static final String CODE_CONVENTION_DEVALID_PEDAGOGIQUE = "CONVENTION_DEVALID_PEDAGOGIQUE";
    public static final String CODE_CONVENTION_VERIF_ADMINISTRATIVE = "CONVENTION_VERIF_ADMINISTRATIVE";
    public static final String CODE_CONVENTION_DEVERIF_ADMINISTRATIVE = "CONVENTION_DEVERIF_ADMINISTRATIVE";
    public static final String CODE_CONVENTION_SIGNEE = "CONVENTION_SIGNEE";
    public static final String CODE_ETU_CREA_AVENANT = "ETU_CREA_AVENANT";
    public static final String CODE_ETU_CREA_CONVENTION = "ETU_CREA_CONVENTION";
    public static final String CODE_ETU_MODIF_AVENANT = "ETU_MODIF_AVENANT";
    public static final String CODE_ETU_MODIF_CONVENTION = "ETU_MODIF_CONVENTION";
    public static final String CODE_GES_CREA_AVENANT = "GES_CREA_AVENANT";
    public static final String CODE_GES_CREA_CONVENTION = "GES_CREA_CONVENTION";
    public static final String CODE_GES_MODIF_AVENANT = "GES_MODIF_AVENANT";
    public static final String CODE_GES_MODIF_CONVENTION = "GES_MODIF_CONVENTION";
    public static final String CODE_FICHE_EVAL_ETU = "FICHE_EVAL_ETU";
    public static final String CODE_FICHE_EVAL_ENSEIGNANT = "FICHE_EVAL_ENSEIGNANT";
    public static final String CODE_FICHE_EVAL_TUTEUR = "FICHE_EVAL_TUTEUR";
    public static final String CODE_RAPPEL_FICHE_EVAL_ETU = "RAPPEL_FICHE_EVAL_ETU";
    public static final String CODE_RAPPEL_FICHE_EVAL_ENSEIGNANT = "RAPPEL_FICHE_EVAL_ENSEIGNANT";
    public static final String CODE_RAPPEL_FICHE_EVAL_TUTEUR = "RAPPEL_FICHE_EVAL_TUTEUR";
    public static final String CODE_CHANGEMENT_ENSEIGNANT = "CHANGEMENT_ENSEIGNANT";
    public static final String CODE_EVAL_TUTEUR_REMPLIE = "EVAL_TUTEUR_REMPLIE";
    public static final String CODE_EVAL_ENSEIGNANT_REMPLIE = "EVAL_ENS_REMPLIE";
    public static final String CODE_EVAL_ETU_REMPLIE = "EVAL_ETU_REMPLIE";
    public static final String CODE_EVAL_REMPLIES = "EVAL_REMPLIES";


    @Id
    @Column(nullable = false)
    private int id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String libelle;

    @Column(nullable = false)
    private String objet;

    @Column(nullable = false)
    private String texte;

    @Override
    public String getExportValue(String key) {
        String value = "";
        switch (key) {
            case "code":
                value = getCode();
                break;
            case "libelle":
                value = getLibelle();
                break;
            case "objet":
                value = getObjet();
                break;
            case "texte":
                value = getTexte();
                break;
            default:
                break;
        }
        return value;
    }
}
