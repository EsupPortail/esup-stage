package org.esup_portail.esup_stage.controller;

import jakarta.validation.Valid;
import org.esup_portail.esup_stage.dto.ConventionEvaluationTuteurDto;
import org.esup_portail.esup_stage.dto.ReponseEntrepriseFormDto;
import org.esup_portail.esup_stage.dto.ReponseSupplementaireFormDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.service.evaluation.EvaluationService;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;

@ApiController
@RequestMapping("/evaluation-tuteur")
public class EvaluationTuteurController {

    private static final Logger logger = LogManager.getLogger(EvaluationTuteurController.class);

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private QuestionSupplementaireJpaRepository QSJpaRepository;

    @Autowired
    private ReponseEvaluationJpaRepository reponseEvaluationJpaRepository;

    @Autowired
    private ReponseSupplementaireJpaRepository reponseSupplementaireJpaRepository;

    @Autowired
    private ConventionJpaRepository conventionJpaRepository;

    @Autowired
    private CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    private ImpressionService impressionService;

    @GetMapping("/access")
    public ConventionEvaluationTuteurDto accessEvaluationPage(@RequestParam(name = "token") String token) {

        if(token == null || token.trim().isEmpty()) {
            logger.warn("Tentative d'accès avec token null ou vide");
            throw new AppException(HttpStatus.FORBIDDEN, "Token manquant");
        }

        // Validation du token
        EvaluationTuteurToken validToken = evaluationService.validateToken(token);

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

    @PostMapping("/{id}")
    public ReponseEvaluation createReponseEntreprise(@RequestParam(name = "token") String token,@PathVariable("id") int id, @Valid @RequestBody ReponseEntrepriseFormDto reponseEntrepriseFormDto) {
        checkToken(token);
        ReponseEvaluation reponseEvaluation = evaluationService.initReponseEvaluation(id);
        evaluationService.setReponseEvaluationEntrepriseData(reponseEvaluation, reponseEntrepriseFormDto);
        return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
    }

    @PutMapping("/{id}")
    public ReponseEvaluation updateReponseEntreprise(@RequestParam(name = "token") String token,@PathVariable("id") int id, @Valid @RequestBody ReponseEntrepriseFormDto reponseEntrepriseFormDto) {
        checkToken(token);
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findByConvention(id);
        if (reponseEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseEvaluation non trouvé");
        }
        evaluationService.setReponseEvaluationEntrepriseData(reponseEvaluation, reponseEntrepriseFormDto);
        return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
    }

    @PostMapping("/{idConvention}/reponseSupplementaire/{idQestion}")
    public ReponseSupplementaire createReponseSupplementaire(@RequestParam(name = "token") String token,@PathVariable("idConvention") int idConvention, @PathVariable("idQestion") int idQestion, @Valid @RequestBody ReponseSupplementaireFormDto reponseSupplementaireFormDto) {
        checkToken(token);
        ReponseSupplementaire reponseSupplementaire = evaluationService.initReponseSupplementaire(idConvention, idQestion);
        evaluationService.setReponseSupplementaireData(reponseSupplementaire, reponseSupplementaireFormDto);
        return reponseSupplementaireJpaRepository.saveAndFlush(reponseSupplementaire);
    }

    @PutMapping("/{idConvention}/reponseSupplementaire/{idQestion}")
    public ReponseSupplementaire updateReponseSupplementaire(@RequestParam(name = "token") String token,@PathVariable("idConvention") int idConvention, @PathVariable("idQestion") int idQestion, @Valid @RequestBody ReponseSupplementaireFormDto reponseSupplementaireFormDto) {
        checkToken(token);
        ReponseSupplementaire reponseSupplementaire = reponseSupplementaireJpaRepository.findByQuestionAndConvention(idConvention, idQestion);
        if (reponseSupplementaire == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseSupplementaire non trouvé");
        }
        evaluationService.setReponseSupplementaireData(reponseSupplementaire, reponseSupplementaireFormDto);
        return reponseSupplementaireJpaRepository.saveAndFlush(reponseSupplementaire);
    }

    @PostMapping("/{id}/validate/{valid}")
    public boolean validate(@RequestParam(name = "token") String token, @PathVariable("id") int id,@PathVariable("valid") boolean valid){
        if(token == null || token.trim().isEmpty()) {
            logger.warn("Tentative d'accès avec token null ou vide");
            throw new AppException(HttpStatus.FORBIDDEN, "Token manquant");
        }
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findByConvention(id);
        if (reponseEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseEvaluation non trouvé");
        }
        EvaluationTuteurToken validToken = evaluationService.validateAndUseToken(token);
        reponseEvaluation.setValidationEntreprise(valid);
        if (validToken == null) {
            throw new AppException(HttpStatus.FORBIDDEN, "Token invalide, expiré ou déjà utilisé");
        }
        return true;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePDF(@RequestParam(name = "token") String token, @PathVariable(name="id") Integer id) {
        checkUsedToken(token);
        Convention convention = conventionJpaRepository.findById(id).orElse(null);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        if (convention.getNomEtabRef() == null || convention.getAdresseEtabRef() == null) {
            CentreGestion centreGestionEtab = centreGestionJpaRepository.getCentreEtablissement();
            // Erreur si le centre de type etablissement est null
            if (centreGestionEtab == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Centre de gestion de type établissement non trouvé");
            }
            convention.setNomEtabRef(centreGestionEtab.getNomCentre());
            convention.setAdresseEtabRef(centreGestionEtab.getAdresseComplete());
            conventionJpaRepository.saveAndFlush(convention);
        }
        ByteArrayOutputStream ou = new ByteArrayOutputStream();
        impressionService.generateEvaluationTuteurPDF(convention, null, ou);

        byte[] pdf = ou.toByteArray();
        return ResponseEntity.ok().body(pdf);
    }

    private void checkToken(String token){
        if(token == null || token.trim().isEmpty()) {
            throw new AppException(HttpStatus.FORBIDDEN, "Token manquant");
        }

        EvaluationTuteurToken validToken = evaluationService.validateToken(token);

        if (validToken == null) {
            logger.warn("Tentative d'accès avec token invalide: {}",
                    token.substring(0, Math.min(8, token.length())));
            throw new AppException(HttpStatus.FORBIDDEN, "Token invalide ou expiré");
        }
    }

    private void checkUsedToken(String token){
        if(token == null || token.trim().isEmpty()) {
            throw new AppException(HttpStatus.FORBIDDEN, "Token manquant");
        }

        EvaluationTuteurToken validToken = evaluationService.validateUsedToken(token);

        if (validToken == null) {
            logger.warn("Tentative d'accès avec token invalide: {}",
                    token.substring(0, Math.min(8, token.length())));
            throw new AppException(HttpStatus.FORBIDDEN, "Token invalide ou expiré");
        }
    }
}
