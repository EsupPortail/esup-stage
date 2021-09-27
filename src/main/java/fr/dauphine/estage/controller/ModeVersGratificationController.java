package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.ModeVersGratification;
import fr.dauphine.estage.repository.ConventionJpaRepository;
import fr.dauphine.estage.repository.ModeVersGratificationJpaRepository;
import fr.dauphine.estage.repository.ModeVersGratificationRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/mode-vers-gratification")
public class ModeVersGratificationController {

    @Autowired
    ModeVersGratificationRepository modeVersGratificationRepository;

    @Autowired
    ModeVersGratificationJpaRepository modeVersGratificationJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<ModeVersGratification> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<ModeVersGratification> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(modeVersGratificationRepository.count(filters));
        paginatedResponse.setData(modeVersGratificationRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public ModeVersGratification update(@PathVariable("id") int id, @RequestBody ModeVersGratification requestModeVersGratification) {
        ModeVersGratification modeVersGratification = modeVersGratificationJpaRepository.findById(id);

        modeVersGratification.setLibelle(requestModeVersGratification.getLibelle());
        if (requestModeVersGratification.getTemEnServ() != null) {
            modeVersGratification.setTemEnServ(requestModeVersGratification.getTemEnServ());
        }
        modeVersGratification = modeVersGratificationJpaRepository.saveAndFlush(modeVersGratification);
        return modeVersGratification;
    }

    @DeleteMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithModeVersGratification(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        modeVersGratificationJpaRepository.deleteById(id);
        modeVersGratificationJpaRepository.flush();
    }
}
