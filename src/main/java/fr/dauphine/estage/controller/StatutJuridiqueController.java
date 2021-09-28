package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.StatutJuridique;
import fr.dauphine.estage.repository.ConventionJpaRepository;
import fr.dauphine.estage.repository.StatutJuridiqueJpaRepository;
import fr.dauphine.estage.repository.StatutJuridiqueRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/statut-juridique")
public class StatutJuridiqueController {

    @Autowired
    StatutJuridiqueRepository statutJuridiqueRepository;

    @Autowired
    StatutJuridiqueJpaRepository statutJuridiqueJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<StatutJuridique> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<StatutJuridique> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(statutJuridiqueRepository.count(filters));
        paginatedResponse.setData(statutJuridiqueRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public StatutJuridique update(@PathVariable("id") int id, @RequestBody StatutJuridique requestStatutJuridique) {
        StatutJuridique statutJuridique = statutJuridiqueJpaRepository.findById(id);

        statutJuridique.setLibelle(requestStatutJuridique.getLibelle());
        if (requestStatutJuridique.getTemEnServ() != null) {
            statutJuridique.setTemEnServ(requestStatutJuridique.getTemEnServ());
        }
        statutJuridique = statutJuridiqueJpaRepository.saveAndFlush(statutJuridique);
        return statutJuridique;
    }

    @DeleteMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithStatutJuridique(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        statutJuridiqueJpaRepository.deleteById(id);
        statutJuridiqueJpaRepository.flush();
    }
}
