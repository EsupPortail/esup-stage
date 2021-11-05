package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Devise;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.DeviseJpaRepository;
import org.esup_portail.esup_stage.repository.DeviseRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
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
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Devise> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Devise> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(deviseRepository.count(filters));
        paginatedResponse.setData(deviseRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.CREATION})
    public Devise create(@RequestBody Devise devise) {
        if (deviseRepository.exists(devise.getLibelle(), devise.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Libellé déjà existant");
        }
        devise.setTemEnServ("O");
        devise = deviseJpaRepository.saveAndFlush(devise);
        return devise;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
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
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithDevise(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        deviseJpaRepository.deleteById(id);
        deviseJpaRepository.flush();
    }
}
