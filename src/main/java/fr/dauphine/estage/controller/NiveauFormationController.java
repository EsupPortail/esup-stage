package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.NiveauFormation;
import fr.dauphine.estage.repository.ConventionJpaRepository;
import fr.dauphine.estage.repository.NiveauFormationJpaRepository;
import fr.dauphine.estage.repository.NiveauFormationRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/niveau-formation")
public class NiveauFormationController {

    @Autowired
    NiveauFormationRepository niveauFormationRepository;

    @Autowired
    NiveauFormationJpaRepository niveauFormationJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<NiveauFormation> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<NiveauFormation> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(niveauFormationRepository.count(filters));
        paginatedResponse.setData(niveauFormationRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PostMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.CREATION})
    public NiveauFormation create(@RequestBody NiveauFormation niveauFormation) {
        if (niveauFormationRepository.exists(niveauFormation.getLibelle(), niveauFormation.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Libellé déjà existant");
        }
        niveauFormation.setTemEnServ("O");
        niveauFormation.setModifiable(true);
        niveauFormation = niveauFormationJpaRepository.saveAndFlush(niveauFormation);
        return niveauFormation;
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public NiveauFormation update(@PathVariable("id") int id, @RequestBody NiveauFormation requestNiveauFormation) {
        NiveauFormation niveauFormation = niveauFormationJpaRepository.findById(id);

        niveauFormation.setLibelle(requestNiveauFormation.getLibelle());
        if (requestNiveauFormation.getTemEnServ() != null) {
            niveauFormation.setTemEnServ(requestNiveauFormation.getTemEnServ());
        }
        niveauFormation = niveauFormationJpaRepository.saveAndFlush(niveauFormation);
        return niveauFormation;
    }

    @DeleteMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithNiveauFormation(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        niveauFormationJpaRepository.deleteById(id);
        niveauFormationJpaRepository.flush();
    }
}
