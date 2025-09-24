package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConventionEvaluationTuteurDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.EvaluationTuteurToken;
import org.esup_portail.esup_stage.repository.EvaluationTuteurTokenRepository;
import org.esup_portail.esup_stage.repository.QuestionSupplementaireJpaRepository;
import org.esup_portail.esup_stage.service.evaluation.EvaluationTuteurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ApiController
@RequestMapping("/evaluation-tuteur")
public class EvaluationTuteurController {

    private static final Logger logger = LogManager.getLogger(EvaluationTuteurController.class);

    @Autowired
    private EvaluationTuteurService evaluationTuteurService;

    @Autowired
    private QuestionSupplementaireJpaRepository QSJpaRepository;

    @Autowired
    private EvaluationTuteurTokenRepository evaluationTuteurTokenRepository;

    @GetMapping("/access")
    public ConventionEvaluationTuteurDto accessEvaluationPage(@RequestParam(name = "token") String token) {

        if(token == null || token.trim().isEmpty()) {
            logger.warn("Tentative d'accès avec token null ou vide");
            throw new AppException(HttpStatus.FORBIDDEN, "Token manquant");
        }

        // Validation du token
        EvaluationTuteurToken validToken = evaluationTuteurService.validateToken(token);

        if (validToken == null) {
            logger.warn("Tentative d'accès avec token invalide: {}",
                    token.substring(0, Math.min(8, token.length())));
            throw new AppException(HttpStatus.FORBIDDEN, "Token invalide ou expiré");
        }
        ConventionEvaluationTuteurDto convention = new ConventionEvaluationTuteurDto(
                validToken.getConvention().getId(),
                validToken.getConvention().getContact(),
                validToken.getConvention().getEtudiant(),
                validToken.getConvention().getCentreGestion().getFicheEvaluation(),
                QSJpaRepository.findByFicheEvaluation(validToken.getConvention().getCentreGestion().getFicheEvaluation().getId()),
                validToken.getConvention().getReponseEvaluation(),
                validToken.getConvention().getReponseSupplementaires());

        return convention;
    }

    @PostMapping("/submit")
    public Boolean submitEvaluation(@RequestParam(name = "token") String token) {

        if(token == null || token.trim().isEmpty()) {
            logger.warn("Tentative d'accès avec token null ou vide");
            throw new AppException(HttpStatus.FORBIDDEN, "Token manquant");
        }

        // Re-valider le token pour la soumission
        EvaluationTuteurToken validToken = evaluationTuteurService.validateAndUseToken(token);

        if (validToken == null) {
            throw new AppException(HttpStatus.FORBIDDEN, "Token invalide, expiré ou déjà utilisé");
        }

        return true;
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }
}
