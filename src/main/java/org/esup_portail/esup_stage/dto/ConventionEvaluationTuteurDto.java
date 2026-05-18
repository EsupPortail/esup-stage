package org.esup_portail.esup_stage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.esup_portail.esup_stage.model.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConventionEvaluationTuteurDto {
    private Integer id;
    private ContactDto tuteur;
    private PersonneDto etudiant;
    private FicheEvaluation ficheEvaluation;
    private List<QuestionSupplementaire> questionSupplementaire;
    private ReponseEvaluation reponseEvaluation;
    private List<ReponseSupplementaire> reponseSupplementaire;

    public ConventionEvaluationTuteurDto(Integer id, Contact tuteur, Etudiant etudiant, FicheEvaluation ficheEvaluation, List<QuestionSupplementaire> questionSupplementaire, ReponseEvaluation reponseEvaluation, List<ReponseSupplementaire> reponseSupplementaire) {
        this.id = id;
        this.tuteur = new ContactDto(tuteur);
        this.etudiant = new PersonneDto(etudiant);
        this.ficheEvaluation = ficheEvaluation;
        this.questionSupplementaire = questionSupplementaire;
        this.reponseEvaluation = reponseEvaluation;
        this.reponseSupplementaire = reponseSupplementaire;
    }
}
