package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.TypeStructure;
import fr.dauphine.estage.repository.ConventionJpaRepository;
import fr.dauphine.estage.repository.TypeStructureJpaRepository;
import fr.dauphine.estage.repository.TypeStructureRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/type-structure")
public class TypeStructureController {

    @Autowired
    TypeStructureRepository typeStructureRepository;

    @Autowired
    TypeStructureJpaRepository typeStructureJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<TypeStructure> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<TypeStructure> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(typeStructureRepository.count(filters));
        paginatedResponse.setData(typeStructureRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public TypeStructure update(@PathVariable("id") int id, @RequestBody TypeStructure requestTypeStructure) {
        TypeStructure typeStructure = typeStructureJpaRepository.findById(id);

        typeStructure.setLibelle(requestTypeStructure.getLibelle());
        if (requestTypeStructure.getTemEnServ() != null) {
            typeStructure.setTemEnServ(requestTypeStructure.getTemEnServ());
        }
        typeStructure = typeStructureJpaRepository.saveAndFlush(typeStructure);
        return typeStructure;
    }

    @DeleteMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithTypeStructure(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        typeStructureJpaRepository.deleteById(id);
        typeStructureJpaRepository.flush();
    }
}
