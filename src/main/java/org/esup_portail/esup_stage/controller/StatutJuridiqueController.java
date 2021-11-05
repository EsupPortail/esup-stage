package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.StatutJuridique;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.StatutJuridiqueJpaRepository;
import org.esup_portail.esup_stage.repository.StatutJuridiqueRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
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
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<StatutJuridique> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<StatutJuridique> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(statutJuridiqueRepository.count(filters));
        paginatedResponse.setData(statutJuridiqueRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.CREATION})
    public StatutJuridique create(@RequestBody StatutJuridique statutJuridique) {
        if (statutJuridiqueRepository.exists(statutJuridique.getLibelle(), statutJuridique.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Libellé déjà existant");
        }
        statutJuridique.setTemEnServ("O");
        statutJuridique.setModifiable(true);
        statutJuridique = statutJuridiqueJpaRepository.saveAndFlush(statutJuridique);
        return statutJuridique;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
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
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithStatutJuridique(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        statutJuridiqueJpaRepository.deleteById(id);
        statutJuridiqueJpaRepository.flush();
    }
}
