package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.TypeStructure;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.TypeStructureJpaRepository;
import org.esup_portail.esup_stage.repository.TypeStructureRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
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
    @Secure()
    public PaginatedResponse<TypeStructure> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<TypeStructure> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(typeStructureRepository.count(filters));
        paginatedResponse.setData(typeStructureRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.CREATION})
    public TypeStructure create(@RequestBody TypeStructure typeStructure) {
        if (typeStructureRepository.exists(typeStructure.getLibelle(), typeStructure.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Libellé déjà existant");
        }
        typeStructure.setTemEnServ("O");
        typeStructure.setModifiable(true);
        typeStructure = typeStructureJpaRepository.saveAndFlush(typeStructure);
        return typeStructure;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
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
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithTypeStructure(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        typeStructureJpaRepository.deleteById(id);
        typeStructureJpaRepository.flush();
    }
}
