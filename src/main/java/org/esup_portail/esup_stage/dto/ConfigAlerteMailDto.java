package org.esup_portail.esup_stage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigAlerteMailDto {

    private Alerte alerteEtudiant = new Alerte(true, false, true, true, true, false, true, true, true, true, true, true);
    private Alerte alerteGestionnaire = new Alerte(true, true, false, false, true, true, false, false, true, false, false, false);
    private Alerte alerteRespGestionnaire = new Alerte(false, false, false, false, false, false, false, false, false, false, false, false);
    private Alerte alerteEnseignant = new Alerte(false, false, false, false, false, false, false, false, true, false, false, false);

    public Alerte getAlerteEtudiant() {
        return alerteEtudiant;
    }

    public void setAlerteEtudiant(Alerte alerteEtudiant) {
        this.alerteEtudiant = alerteEtudiant;
    }

    public Alerte getAlerteGestionnaire() {
        return alerteGestionnaire;
    }

    public void setAlerteGestionnaire(Alerte alerteGestionnaire) {
        this.alerteGestionnaire = alerteGestionnaire;
    }

    public Alerte getAlerteRespGestionnaire() {
        return alerteRespGestionnaire;
    }

    public void setAlerteRespGestionnaire(Alerte alerteRespGestionnaire) {
        this.alerteRespGestionnaire = alerteRespGestionnaire;
    }

    public Alerte getAlerteEnseignant() {
        return alerteEnseignant;
    }

    public void setAlerteEnseignant(Alerte alerteEnseignant) {
        this.alerteEnseignant = alerteEnseignant;
    }

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

        public Alerte() { }

        public Alerte(boolean creationConventionEtudiant,
                      boolean modificationConventionEtudiant,
                      boolean creationConventionGestionnaire,
                      boolean modificationConventionGestionnaire,
                      boolean creationAvenantEtudiant,
                      boolean modificationAvenantEtudiant,
                      boolean creationAvenantGestionnaire,
                      boolean modificationAvenantGestionnaire,
                      boolean validationPedagogiqueConvention,
                      boolean validationAdministrativeConvention,
                      boolean verificationAdministrativeConvention,
                      boolean validationAvenant)
        {
            this.creationConventionEtudiant = creationConventionEtudiant;
            this.modificationConventionEtudiant = modificationConventionEtudiant;
            this.creationConventionGestionnaire = creationConventionGestionnaire;
            this.modificationConventionGestionnaire = modificationConventionGestionnaire;
            this.creationAvenantEtudiant = creationAvenantEtudiant;
            this.modificationAvenantEtudiant = modificationAvenantEtudiant;
            this.creationAvenantGestionnaire = creationAvenantGestionnaire;
            this.modificationAvenantGestionnaire = modificationAvenantGestionnaire;
            this.validationPedagogiqueConvention = validationPedagogiqueConvention;
            this.validationAdministrativeConvention = validationAdministrativeConvention;
            this.verificationAdministrativeConvention = verificationAdministrativeConvention;
            this.validationAvenant = validationAvenant;
        }

        public boolean isCreationConventionEtudiant() {
            return creationConventionEtudiant;
        }

        public void setCreationConventionEtudiant(boolean creationConventionEtudiant) {
            this.creationConventionEtudiant = creationConventionEtudiant;
        }

        public boolean isModificationConventionEtudiant() {
            return modificationConventionEtudiant;
        }

        public void setModificationConventionEtudiant(boolean modificationConventionEtudiant) {
            this.modificationConventionEtudiant = modificationConventionEtudiant;
        }

        public boolean isCreationConventionGestionnaire() {
            return creationConventionGestionnaire;
        }

        public void setCreationConventionGestionnaire(boolean creationConventionGestionnaire) {
            this.creationConventionGestionnaire = creationConventionGestionnaire;
        }

        public boolean isModificationConventionGestionnaire() {
            return modificationConventionGestionnaire;
        }

        public void setModificationConventionGestionnaire(boolean modificationConventionGestionnaire) {
            this.modificationConventionGestionnaire = modificationConventionGestionnaire;
        }

        public boolean isCreationAvenantEtudiant() {
            return creationAvenantEtudiant;
        }

        public void setCreationAvenantEtudiant(boolean creationAvenantEtudiant) {
            this.creationAvenantEtudiant = creationAvenantEtudiant;
        }

        public boolean isModificationAvenantEtudiant() {
            return modificationAvenantEtudiant;
        }

        public void setModificationAvenantEtudiant(boolean modificationAvenantEtudiant) {
            this.modificationAvenantEtudiant = modificationAvenantEtudiant;
        }

        public boolean isCreationAvenantGestionnaire() {
            return creationAvenantGestionnaire;
        }

        public void setCreationAvenantGestionnaire(boolean creationAvenantGestionnaire) {
            this.creationAvenantGestionnaire = creationAvenantGestionnaire;
        }

        public boolean isModificationAvenantGestionnaire() {
            return modificationAvenantGestionnaire;
        }

        public void setModificationAvenantGestionnaire(boolean modificationAvenantGestionnaire) {
            this.modificationAvenantGestionnaire = modificationAvenantGestionnaire;
        }

        public boolean isValidationPedagogiqueConvention() {
            return validationPedagogiqueConvention;
        }

        public void setValidationPedagogiqueConvention(boolean validationPedagogiqueConvention) {
            this.validationPedagogiqueConvention = validationPedagogiqueConvention;
        }

        public boolean isValidationAdministrativeConvention() {
            return validationAdministrativeConvention;
        }

        public void setValidationAdministrativeConvention(boolean validationAdministrativeConvention) {
            this.validationAdministrativeConvention = validationAdministrativeConvention;
        }

        public boolean isVerificationAdministrativeConvention() {
            return verificationAdministrativeConvention;
        }

        public void setVerificationAdministrativeConvention(boolean verificationAdministrativeConvention) {
            this.verificationAdministrativeConvention = verificationAdministrativeConvention;
        }

        public boolean isValidationAvenant() {
            return validationAvenant;
        }

        public void setValidationAvenant(boolean validationAvenant) {
            this.validationAvenant = validationAvenant;
        }
    }
}
