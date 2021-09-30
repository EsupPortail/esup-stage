package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.OrigineStage;
import fr.dauphine.estage.repository.ConventionJpaRepository;
import fr.dauphine.estage.repository.OrigineStageJpaRepository;
import fr.dauphine.estage.repository.OrigineStageRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/origine-stage")
public class OrigineStageController {

    @Autowired
    OrigineStageRepository origineStageRepository;

    @Autowired
    OrigineStageJpaRepository origineStageJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<OrigineStage> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<OrigineStage> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(origineStageRepository.count(filters));
        paginatedResponse.setData(origineStageRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public OrigineStage update(@PathVariable("id") int id, @RequestBody OrigineStage requestOrigineStage) {
        OrigineStage origineStage = origineStageJpaRepository.findById(id);

        origineStage.setLibelle(requestOrigineStage.getLibelle());
        if (requestOrigineStage.getTemEnServ() != null) {
            origineStage.setTemEnServ(requestOrigineStage.getTemEnServ());
        }
        origineStage = origineStageJpaRepository.saveAndFlush(origineStage);
        return origineStage;
    }

    @DeleteMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithOrigineStage(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        origineStageJpaRepository.deleteById(id);
        origineStageJpaRepository.flush();
    }
}
