package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.Devise;
import fr.dauphine.estage.repository.ConventionJpaRepository;
import fr.dauphine.estage.repository.DeviseJpaRepository;
import fr.dauphine.estage.repository.DeviseRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/devise")
public class DeviseController {

    @Autowired
    DeviseRepository deviseRepository;

    @Autowired
    DeviseJpaRepository deviseJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Devise> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Devise> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(deviseRepository.count(filters));
        paginatedResponse.setData(deviseRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public Devise update(@PathVariable("id") int id, @RequestBody Devise requestDevise) {
        Devise devise = deviseJpaRepository.findById(id);

        devise.setLibelle(requestDevise.getLibelle());
        if (requestDevise.getTemEnServ() != null) {
            devise.setTemEnServ(requestDevise.getTemEnServ());
        }
        devise = deviseJpaRepository.saveAndFlush(devise);
        return devise;
    }

    @DeleteMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithDevise(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        deviseJpaRepository.deleteById(id);
        deviseJpaRepository.flush();
    }
}
