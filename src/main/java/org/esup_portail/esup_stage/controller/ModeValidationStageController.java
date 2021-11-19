package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.ModeValidationStage;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.ModeValidationStageJpaRepository;
import org.esup_portail.esup_stage.repository.ModeValidationStageRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("mode-validation-stage")
public class ModeValidationStageController {

    @Autowired
    ModeValidationStageRepository modeValidationStageRepository;

    @Autowired
    ModeValidationStageJpaRepository modeValidationStageJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure
    public PaginatedResponse<ModeValidationStage> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<ModeValidationStage> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(modeValidationStageRepository.count(filters));
        paginatedResponse.setData(modeValidationStageRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.CREATION})
    public ModeValidationStage create(@RequestBody ModeValidationStage modeValidationStage) {
        if (modeValidationStageRepository.exists(modeValidationStage.getLibelle(), modeValidationStage.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Libellé déjà existant");
        }
        modeValidationStage.setTemEnServ("O");
        modeValidationStage.setModifiable(true);
        modeValidationStage = modeValidationStageJpaRepository.saveAndFlush(modeValidationStage);
        return modeValidationStage;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public ModeValidationStage update(@PathVariable("id") int id, @RequestBody ModeValidationStage requestModeValidationStage) {
        ModeValidationStage modeValidationStage = modeValidationStageJpaRepository.findById(id);

        modeValidationStage.setLibelle(requestModeValidationStage.getLibelle());
        if (requestModeValidationStage.getTemEnServ() != null) {
            modeValidationStage.setTemEnServ(requestModeValidationStage.getTemEnServ());
        }
        modeValidationStage = modeValidationStageJpaRepository.saveAndFlush(modeValidationStage);
        return modeValidationStage;
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithModeValidationStage(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        modeValidationStageJpaRepository.deleteById(id);
        modeValidationStageJpaRepository.flush();
    }
}
