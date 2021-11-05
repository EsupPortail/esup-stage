package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Contenu;
import org.esup_portail.esup_stage.repository.ContenuJpaRepository;
import org.esup_portail.esup_stage.repository.ContenuRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiController
@RequestMapping("/contenus")
public class ContenuController {

    @Autowired
    ContenuRepository contenuRepository;

    @Autowired
    ContenuJpaRepository contenuJpaRepository;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Contenu> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        PaginatedResponse<Contenu> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(contenuRepository.count(filters));
        paginatedResponse.setData(contenuRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/libelle")
    @Secure
    public List<Contenu> getLibelle() {
        return contenuJpaRepository.getLibelle();
    }

    @GetMapping("/{code}")
    @Secure
    public Contenu get(@PathVariable("code") String code) {
        return contenuJpaRepository.findByCode(code);
    }

    @PutMapping("/{code}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public Contenu update(@PathVariable("code") String code, @RequestBody Contenu contenuRequest) {
        Contenu contenu = contenuJpaRepository.findByCode(code);
        if (contenu == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Contenu " + code + " non trouvé");
        }
        contenu.setLibelle(contenuRequest.isLibelle());
        contenu.setTexte(contenuRequest.getTexte());
        contenu = contenuJpaRepository.saveAndFlush(contenu);
        return contenu;
    }
}
