package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.TypeOffre;
import fr.dauphine.estage.repository.ConventionJpaRepository;
import fr.dauphine.estage.repository.TypeOffreJpaRepository;
import fr.dauphine.estage.repository.TypeOffreRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/type-offre")
public class TypeOffreController {

    @Autowired
    TypeOffreRepository typeOffreRepository;

    @Autowired
    TypeOffreJpaRepository typeOffreJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<TypeOffre> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<TypeOffre> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(typeOffreRepository.count(filters));
        paginatedResponse.setData(typeOffreRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public TypeOffre update(@PathVariable("id") int id, @RequestBody TypeOffre requestTypeOffre) {
        TypeOffre typeOffre = typeOffreJpaRepository.findById(id);

        typeOffre.setLibelle(requestTypeOffre.getLibelle());
        if (requestTypeOffre.getTemEnServ() != null) {
            typeOffre.setTemEnServ(requestTypeOffre.getTemEnServ());
        }
        typeOffre = typeOffreJpaRepository.saveAndFlush(typeOffre);
        return typeOffre;
    }

    @DeleteMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithTypeOffre(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        typeOffreJpaRepository.deleteById(id);
        typeOffreJpaRepository.flush();
    }
}
