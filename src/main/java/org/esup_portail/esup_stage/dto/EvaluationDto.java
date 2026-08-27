package org.esup_portail.esup_stage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.esup_portail.esup_stage.model.*;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationDto {

    private Integer idConvention;
    private Etudiant etudiant;
    private Structure structure;
    private Date dateDebutStage;
    private Date dateFinStage;
    private CentreGestion centreGestion;
    private Etape etape;
    private String anneeUniversitaire;
    private FicheEvaluation ficheEvaluation;
    private ReponseEvaluation reponseEvaluation;
    private List<QuestionSupplementaire> questionSupplementaires;
    private List<ReponseSupplementaire> reponseSupplementaires;

    public EvaluationDto(Convention convention, List<QuestionSupplementaire> questionSupplementaires) {
        this.idConvention = convention.getId();
        this.etudiant = convention.getEtudiant();
        this.structure = convention.getStructure();
        this.dateDebutStage = convention.getDateDebutStage();
        this.dateFinStage = convention.getDateFinStage();
        this.centreGestion = convention.getCentreGestion();
        this.etape = convention.getEtape();
        this.anneeUniversitaire = convention.getAnnee();
        this.ficheEvaluation = convention.getCentreGestion().getFicheEvaluation();
        this.reponseEvaluation = convention.getReponseEvaluation();
        this.questionSupplementaires = questionSupplementaires;
        this.reponseSupplementaires = convention.getReponseSupplementaires();
    }

}
