package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.ModeValidationStage;
import fr.dauphine.estage.repository.ConventionJpaRepository;
import fr.dauphine.estage.repository.ModeValidationStageJpaRepository;
import fr.dauphine.estage.repository.ModeValidationStageRepository;
import fr.dauphine.estage.security.interceptor.Secure;
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
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<ModeValidationStage> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<ModeValidationStage> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(modeValidationStageRepository.count(filters));
        paginatedResponse.setData(modeValidationStageRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
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
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithModeValidationStage(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        modeValidationStageJpaRepository.deleteById(id);
        modeValidationStageJpaRepository.flush();
    }
}
