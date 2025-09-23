package org.esup_portail.esup_stage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.esup_portail.esup_stage.model.*;

import java.util.List;

@Data
@AllArgsConstructor
public class ConventionEvaluationTuteurDto {
    private Integer id;
    private Contact tuteur;
    private Etudiant etudiant;
    private CentreGestion centreGestion;
    private ReponseEvaluation reponseEvaluation;
    private List<ReponseSupplementaire> reponseSupplementaire;
}
