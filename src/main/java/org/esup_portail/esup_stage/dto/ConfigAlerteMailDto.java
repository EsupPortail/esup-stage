package org.esup_portail.esup_stage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.esup_portail.esup_stage.model.TemplateMail;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ConfigAlerteMailDto {

    private Alerte alerteEtudiant = new Alerte(true, false, true, true, true, false, true, true, true, true, true, true, false, true, false, false, false, false);
    private Alerte alerteGestionnaire = new Alerte(true, true, false, false, true, true, false, false, true, false, false, false, true, true, true, true, true, true);
    private Alerte alerteRespGestionnaire = new Alerte(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true);
    private Alerte alerteEnseignant = new Alerte(false, false, false, false, false, false, false, false, true, false, false, false, false, true, false, false, false, false);

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Alerte {
        private boolean creationConventionEtudiant;
        private boolean modificationConventionEtudiant;
        private boolean creationConventionGestionnaire;
        private boolean modificationConventionGestionnaire;
        private boolean creationAvenantEtudiant;
        private boolean modificationAvenantEtudiant;
        private boolean creationAvenantGestionnaire;
        private boolean modificationAvenantGestionnaire;
        private boolean validationPedagogiqueConvention;
        private boolean validationAdministrativeConvention;
        private boolean verificationAdministrativeConvention;
        private boolean validationAvenant;
        private boolean conventionSignee;
        private boolean changementEnseignant;
        private boolean evalTuteurRemplie;
        private boolean evalEnsRemplie;
        private boolean evalEtuRemplie;
        private boolean evalRemplies;

    }

    /* ==== API demandée ==== */
    public boolean isSendToEtudiant(String templateCode) {
        return resolve(alerteEtudiant, templateCode);
    }

    public boolean isSendToEnseignant(String templateCode) {
        return resolve(alerteEnseignant, templateCode);
    }

    public boolean isSendToGestionnaire(String templateCode) {
        return resolve(alerteGestionnaire, templateCode);
    }

    public boolean isSendToRespGestionnaire(String templateCode) {
        return resolve(alerteRespGestionnaire, templateCode);
    }

    /* ==== Moteur de résolution commun ==== */
    private boolean resolve(Alerte a, String templateCode) {
        if (a == null || templateCode == null) return false;
        switch (templateCode) {
            case TemplateMail.CODE_ETU_CREA_CONVENTION:
                return a.isCreationConventionEtudiant();
            case TemplateMail.CODE_ETU_MODIF_CONVENTION:
                return a.isModificationConventionEtudiant();

            case TemplateMail.CODE_GES_CREA_CONVENTION:
                return a.isCreationConventionGestionnaire();
            case TemplateMail.CODE_GES_MODIF_CONVENTION:
                return a.isModificationConventionGestionnaire();

            case TemplateMail.CODE_ETU_CREA_AVENANT:
                return a.isCreationAvenantEtudiant();
            case TemplateMail.CODE_ETU_MODIF_AVENANT:
                return a.isModificationAvenantEtudiant();

            case TemplateMail.CODE_GES_CREA_AVENANT:
                return a.isCreationAvenantGestionnaire();
            case TemplateMail.CODE_GES_MODIF_AVENANT:
                return a.isModificationAvenantGestionnaire();

            case TemplateMail.CODE_CONVENTION_VALID_PEDAGOGIQUE:
                return a.isValidationPedagogiqueConvention();
            case TemplateMail.CODE_CONVENTION_VALID_ADMINISTRATIVE:
                return a.isValidationAdministrativeConvention();
            case TemplateMail.CODE_CONVENTION_VERIF_ADMINISTRATIVE:
                return a.isVerificationAdministrativeConvention();

            case TemplateMail.CODE_AVENANT_VALIDATION:
                return a.isValidationAvenant();

            case TemplateMail.CODE_CONVENTION_SIGNEE:
                return a.isConventionSignee();

            case TemplateMail.CODE_CHANGEMENT_ENSEIGNANT:
                return a.isChangementEnseignant();

            case TemplateMail.CODE_EVAL_TUTEUR_REMPLIE:
                return a.isEvalTuteurRemplie();
            case TemplateMail.CODE_EVAL_ENSEIGNANT_REMPLIE:
                return a.isEvalEnsRemplie();
            case TemplateMail.CODE_EVAL_ETU_REMPLIE:
                return a.isEvalEtuRemplie();
            case TemplateMail.CODE_EVAL_REMPLIES:
                return a.isEvalRemplies();

            default:
                return false;

        }
    }
}
