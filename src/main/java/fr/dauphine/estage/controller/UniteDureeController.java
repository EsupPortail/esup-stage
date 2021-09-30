package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.UniteDuree;
import fr.dauphine.estage.repository.ConventionJpaRepository;
import fr.dauphine.estage.repository.UniteDureeJpaRepository;
import fr.dauphine.estage.repository.UniteDureeRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/unite-duree")
public class UniteDureeController {

    @Autowired
    UniteDureeRepository uniteDureeRepository;

    @Autowired
    UniteDureeJpaRepository uniteDureeJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<UniteDuree> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<UniteDuree> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(uniteDureeRepository.count(filters));
        paginatedResponse.setData(uniteDureeRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public UniteDuree update(@PathVariable("id") int id, @RequestBody UniteDuree requestUniteDuree) {
        UniteDuree uniteDuree = uniteDureeJpaRepository.findById(id);

        uniteDuree.setLibelle(requestUniteDuree.getLibelle());
        if (requestUniteDuree.getTemEnServ() != null) {
            uniteDuree.setTemEnServ(requestUniteDuree.getTemEnServ());
        }
        uniteDuree = uniteDureeJpaRepository.saveAndFlush(uniteDuree);
        return uniteDuree;
    }

    @DeleteMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithUniteDuree(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        uniteDureeJpaRepository.deleteById(id);
        uniteDureeJpaRepository.flush();
    }

}