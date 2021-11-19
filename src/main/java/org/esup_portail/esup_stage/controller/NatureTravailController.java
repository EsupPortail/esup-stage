package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.NatureTravail;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.NatureTravailJpaRepository;
import org.esup_portail.esup_stage.repository.NatureTravailRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/nature-travail")
public class NatureTravailController {

    @Autowired
    NatureTravailRepository natureTravailRepository;

    @Autowired
    NatureTravailJpaRepository natureTravailJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure
    public PaginatedResponse<NatureTravail> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<NatureTravail> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(natureTravailRepository.count(filters));
        paginatedResponse.setData(natureTravailRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.CREATION})
    public NatureTravail create(@RequestBody NatureTravail natureTravail) {
        if (natureTravailRepository.exists(natureTravail.getLibelle(), natureTravail.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Libellé déjà existant");
        }
        natureTravail.setTemEnServ("O");
        natureTravail = natureTravailJpaRepository.saveAndFlush(natureTravail);
        return natureTravail;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public NatureTravail update(@PathVariable("id") int id, @RequestBody NatureTravail requestNatureTravail) {
        NatureTravail natureTravail = natureTravailJpaRepository.findById(id);

        natureTravail.setLibelle(requestNatureTravail.getLibelle());
        if (requestNatureTravail.getTemEnServ() != null) {
            natureTravail.setTemEnServ(requestNatureTravail.getTemEnServ());
        }
        natureTravail = natureTravailJpaRepository.saveAndFlush(natureTravail);
        return natureTravail;
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithNatureTravail(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        natureTravailJpaRepository.deleteById(id);
        natureTravailJpaRepository.flush();
    }
}