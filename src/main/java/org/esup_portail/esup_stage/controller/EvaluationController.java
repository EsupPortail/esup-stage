package org.esup_portail.esup_stage.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.dto.EvaluationDto;
import org.esup_portail.esup_stage.dto.ExcelExportEvalDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.QuestionSupplementaireJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.evaluation.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@ApiController
@RequestMapping("/evaluations")
public class EvaluationController {

    private static final Logger logger = LogManager.getLogger(EvaluationController.class);

    @Autowired
    private ConventionJpaRepository conventionJpaRepository;

    @Autowired
    private QuestionSupplementaireJpaRepository questionSupplementaireJpaRepository;

    @Autowired
    private EvaluationService evaluationService;

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public EvaluationDto getEvaluation(@RequestParam(name = "id") Integer id) {
        Convention convention = conventionJpaRepository.findById(id).orElse(null);
        if (convention == null) {
            logger.debug("Convention with id {} not found", id);
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        return new EvaluationDto(convention,questionSupplementaireJpaRepository.findByFicheEvaluation(convention.getCentreGestion().getFicheEvaluation().getId()));
    }

    @GetMapping()
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public List<EvaluationDto> getEvaluations(@RequestParam("idConventions")List<Integer> idConventions) {
        return getEvalsFromConventions(idConventions);
    }

    @PostMapping(value = "/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportExcel(@RequestBody ExcelExportEvalDto excelExportEvalDto) {

        byte[] bytes = evaluationService.getEvaluationToExcel(getEvalsFromConventions(excelExportEvalDto.getIdConventions()),excelExportEvalDto.getTypeFiche(),excelExportEvalDto.getColonnes());
        return ResponseEntity.ok().body(bytes);
    }

    /* ===================== Helpers ===================== */

    public List<EvaluationDto> getEvalsFromConventions(List<Integer> idConventions) {
        List<EvaluationDto> evals = new ArrayList<>();
        for (Integer id : idConventions) {
            Convention convention = conventionJpaRepository.findById(id).orElse(null);
            if (convention == null) {
                logger.debug("Convention with id {} not found", id);
                throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
            }
            evals.add(new EvaluationDto(convention,questionSupplementaireJpaRepository.findByFicheEvaluation(convention.getCentreGestion().getFicheEvaluation().getId())));
        }
        return evals;
    }

}
