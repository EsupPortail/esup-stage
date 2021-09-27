package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.UniteGratification;
import fr.dauphine.estage.repository.ConventionJpaRepository;
import fr.dauphine.estage.repository.UniteGratificationJpaRepository;
import fr.dauphine.estage.repository.UniteGratificationRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/unite-gratification")
public class UniteGratificationController {

    @Autowired
    UniteGratificationRepository uniteGratificationRepository;

    @Autowired
    UniteGratificationJpaRepository uniteGratificationJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<UniteGratification> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<UniteGratification> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(uniteGratificationRepository.count(filters));
        paginatedResponse.setData(uniteGratificationRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public UniteGratification update(@PathVariable("id") int id, @RequestBody UniteGratification requestUniteGratification) {
        UniteGratification uniteGratification = uniteGratificationJpaRepository.findById(id);

        uniteGratification.setLibelle(requestUniteGratification.getLibelle());
        if (requestUniteGratification.getTemEnServ() != null) {
            uniteGratification.setTemEnServ(requestUniteGratification.getTemEnServ());
        }
        uniteGratification = uniteGratificationJpaRepository.saveAndFlush(uniteGratification);
        return uniteGratification;
    }

    @DeleteMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithUniteGratification(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        uniteGratificationJpaRepository.deleteById(id);
        uniteGratificationJpaRepository.flush();
    }
}
