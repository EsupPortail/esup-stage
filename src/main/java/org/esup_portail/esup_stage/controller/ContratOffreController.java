package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.ContratOffre;
import org.esup_portail.esup_stage.repository.ContratOffreJpaRepository;
import org.esup_portail.esup_stage.repository.ContratOffreRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/contrat-offre")
public class ContratOffreController {

    @Autowired
    ContratOffreRepository contratOffreRepository;

    @Autowired
    ContratOffreJpaRepository contratOffreJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<ContratOffre> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<ContratOffre> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(contratOffreRepository.count(filters));
        paginatedResponse.setData(contratOffreRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PostMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.CREATION})
    public ContratOffre create(@RequestBody ContratOffre contratOffre) {
        if (contratOffreRepository.exists(contratOffre.getLibelle(), contratOffre.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Libellé déjà existant");
        }
        contratOffre.setTemEnServ("O");
        contratOffre.setModifiable(true);
        contratOffre = contratOffreJpaRepository.saveAndFlush(contratOffre);
        return contratOffre;
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public ContratOffre update(@PathVariable("id") int id, @RequestBody ContratOffre requestContratOffre) {
        ContratOffre contratOffre = contratOffreJpaRepository.findById(id);

        contratOffre.setLibelle(requestContratOffre.getLibelle());
        if (requestContratOffre.getTemEnServ() != null) {
            contratOffre.setTemEnServ(requestContratOffre.getTemEnServ());
        }
        contratOffre = contratOffreJpaRepository.saveAndFlush(contratOffre);
        return contratOffre;
    }

    @DeleteMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithContratOffre(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        contratOffreJpaRepository.deleteById(id);
        contratOffreJpaRepository.flush();
    }
}
