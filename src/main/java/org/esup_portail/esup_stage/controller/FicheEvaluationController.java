package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.FicheEvaluationDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.FicheEvaluationJpaRepository;
import org.esup_portail.esup_stage.repository.FicheEvaluationRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@ApiController
@RequestMapping("/ficheEvaluation")
public class FicheEvaluationController {

    @Autowired
    FicheEvaluationRepository ficheEvaluationRepository;

    @Autowired
    FicheEvaluationJpaRepository ficheEvaluationJpaRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<FicheEvaluation> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<FicheEvaluation> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(ficheEvaluationRepository.count(filters));
        paginatedResponse.setData(ficheEvaluationRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public FicheEvaluation getById(@PathVariable("id") int id) {
        FicheEvaluation ficheEvaluation = ficheEvaluationJpaRepository.findById(id);
        if (ficheEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "FicheEvaluation non trouvée");
        }
        return ficheEvaluation;
    }
    @GetMapping("/getByCentreGestion/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public FicheEvaluation getByCentreGestion(@PathVariable("id") int id) {
        FicheEvaluation ficheEvaluation = ficheEvaluationJpaRepository.findByCentreGestion(id);
        if (ficheEvaluation == null) {
            ficheEvaluation = new FicheEvaluation();
            CentreGestion centreGestion = centreGestionJpaRepository.findById(id);
            if (centreGestion == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "CentreGestion non trouvée");
            }
            ficheEvaluation.setCentreGestion(centreGestion);
            return ficheEvaluationJpaRepository.saveAndFlush(ficheEvaluation);
        }
        return ficheEvaluation;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.CREATION})
    public FicheEvaluation create(@Valid @RequestBody FicheEvaluationDto ficheEvaluationDto) {
        FicheEvaluation ficheEvaluation = new FicheEvaluation();
        setFicheEvaluationData(ficheEvaluation, ficheEvaluationDto);
        return ficheEvaluationJpaRepository.saveAndFlush(ficheEvaluation);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public FicheEvaluation update(@PathVariable("id") int id, @Valid @RequestBody FicheEvaluationDto ficheEvaluationDto) {
        FicheEvaluation ficheEvaluation = ficheEvaluationJpaRepository.findById(id);
        if (ficheEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "FicheEvaluation non trouvé");
        }
        setFicheEvaluationData(ficheEvaluation, ficheEvaluationDto);
        return ficheEvaluationJpaRepository.saveAndFlush(ficheEvaluation);
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.SUPPRESSION})
    public boolean delete(@PathVariable("id") int id) {
        FicheEvaluation ficheEvaluation = ficheEvaluationJpaRepository.findById(id);
        if (ficheEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "FicheEvaluation non trouvé");
        }
        ficheEvaluationJpaRepository.delete(ficheEvaluation);
        ficheEvaluationJpaRepository.flush();
        return true;
    }

    private void setFicheEvaluationData(FicheEvaluation ficheEvaluation, FicheEvaluationDto ficheEvaluationDto) {

    }
}