package org.esup_portail.esup_stage.dto;

import lombok.Data;

import java.util.List;


/**
 * Dto qui permet le résultat de l'envoi en masse de mail pour les évaluations de stage
 */
@Data
public class SendMailEvaluationEnMasseResponseDto {

    private Summary resume;
    private List<Row> conventions;

    @Data
    public static class Summary {
        public int requested;   // nb d'IDs reçus
        public int found;       // nb de conventions trouvées
        public int sent;        // nb d'envois OK
        public int failed;      // nb d'envois en erreur (inclut not_found / no_email)
        public int typeFiche;   // 0=etu,1=ens,2=tuteur
    }

    @Data
    public static class Row {
        public Integer conventionId;
        public String to;               // destinataire effectif
        public String template;         // code template utilisé
        public String status;           // SENT | ERROR
        public String reason;           // not_found | no_email | exception
        public String error;            // message d’erreur si exception
        public boolean rappel;          // vrai si “rappel”
        public boolean toCentreGestion; // envoi redirigé vers le centre
    }
}
