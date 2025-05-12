package org.esup_portail.esup_stage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ConfigAlerteMailDto {

    private Alerte alerteEtudiant = new Alerte(true, false, true, true, true, false, true, true, true, true, true, true, false);
    private Alerte alerteGestionnaire = new Alerte(true, true, false, false, true, true, false, false, true, false, false, false, true);
    private Alerte alerteRespGestionnaire = new Alerte(false, false, false, false, false, false, false, false, false, false, false, false,false);
    private Alerte alerteEnseignant = new Alerte(false, false, false, false, false, false, false, false, true, false, false, false,false);

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

    }
}
