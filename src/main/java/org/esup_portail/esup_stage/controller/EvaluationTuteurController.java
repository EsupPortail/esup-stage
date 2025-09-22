package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.EvaluationTuteurToken;
import org.esup_portail.esup_stage.service.evaluation.EvaluationTuteurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Controller
@RequestMapping("/evaluation-tuteur")
public class EvaluationTuteurController {

    private static final Logger logger = LogManager.getLogger(EvaluationTuteurController.class);

    @Autowired
    private EvaluationTuteurService evaluationTuteurService;

    @GetMapping("/access")
    public Boolean accessEvaluationPage(@RequestParam(name = "token") String token) {

        if(token == null || token.trim().isEmpty()) {
            logger.warn("Tentative d'accès avec token null ou vide");
            throw new AppException(HttpStatus.FORBIDDEN, "Token manquant");
        }

        // Validation du token
        EvaluationTuteurToken validToken = evaluationTuteurService.validateToken(token);

        if (validToken == null) {
            logger.warn("Tentative d'accès avec token invalide: {}",
                    token.substring(0, Math.min(8, token.length())) + "...");
            throw new AppException(HttpStatus.FORBIDDEN, "Token invalide ou expiré");
        }

        return true;
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
}
