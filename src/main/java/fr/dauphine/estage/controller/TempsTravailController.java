package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.TempsTravail;
import fr.dauphine.estage.repository.ConventionJpaRepository;
import fr.dauphine.estage.repository.TempsTravailJpaRepository;
import fr.dauphine.estage.repository.TempsTravailRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/temps-travail")
public class TempsTravailController {

    @Autowired
    TempsTravailRepository tempsTravailRepository;

    @Autowired
    TempsTravailJpaRepository tempsTravailJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<TempsTravail> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<TempsTravail> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(tempsTravailRepository.count(filters));
        paginatedResponse.setData(tempsTravailRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PutMapping("{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public TempsTravail update(@PathVariable("id") int id, @RequestBody TempsTravail requestTempsTravail) {
        TempsTravail tempsTravail = tempsTravailJpaRepository.findById(id);

        tempsTravail.setLibelle(requestTempsTravail.getLibelle());
        if (requestTempsTravail.getTemEnServ() != null) {
            tempsTravail.setTemEnServ(requestTempsTravail.getTemEnServ());
        }
        tempsTravail = tempsTravailJpaRepository.saveAndFlush(tempsTravail);
        return tempsTravail;
    }

    @DeleteMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithTempsTravail(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        tempsTravailJpaRepository.deleteById(id);
        tempsTravailJpaRepository.flush();
    }
}
