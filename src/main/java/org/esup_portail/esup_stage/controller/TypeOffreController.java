package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.TypeOffre;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.TypeOffreJpaRepository;
import org.esup_portail.esup_stage.repository.TypeOffreRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
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
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<TypeOffre> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<TypeOffre> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(typeOffreRepository.count(filters));
        paginatedResponse.setData(typeOffreRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.CREATION})
    public TypeOffre create(@RequestBody TypeOffre typeOffre) {
        if (typeOffreRepository.exists(typeOffre.getCodeCtrl(), typeOffre.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Code déjà existant");
        }
        typeOffre.setTemEnServ("O");
        typeOffre.setModifiable(true);
        typeOffre = typeOffreJpaRepository.saveAndFlush(typeOffre);
        return typeOffre;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
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
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithTypeOffre(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        typeOffreJpaRepository.deleteById(id);
        typeOffreJpaRepository.flush();
    }
}
