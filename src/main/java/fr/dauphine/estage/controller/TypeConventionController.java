package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.TypeConvention;
import fr.dauphine.estage.repository.ConventionJpaRepository;
import fr.dauphine.estage.repository.TypeConventionJpaRepository;
import fr.dauphine.estage.repository.TypeConventionRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/type-convention")
public class TypeConventionController {

    @Autowired
    TypeConventionRepository typeConventionRepository;

    @Autowired
    TypeConventionJpaRepository typeConventionJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<TypeConvention> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<TypeConvention> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(typeConventionRepository.count(filters));
        paginatedResponse.setData(typeConventionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public TypeConvention update(@PathVariable("id") int id, @RequestBody TypeConvention requestTypeConvention) {
        TypeConvention typeConvention = typeConventionJpaRepository.findById(id);

        typeConvention.setLibelle(requestTypeConvention.getLibelle());
        if (requestTypeConvention.getTemEnServ() != null) {
            typeConvention.setTemEnServ(requestTypeConvention.getTemEnServ());
        }
        typeConvention = typeConventionJpaRepository.saveAndFlush(typeConvention);
        return typeConvention;
    }

    @DeleteMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithTypeConvention(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        typeConventionJpaRepository.deleteById(id);
        typeConventionJpaRepository.flush();
    }
}
