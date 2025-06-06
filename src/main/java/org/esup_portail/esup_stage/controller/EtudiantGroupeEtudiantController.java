package org.esup_portail.esup_stage.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.model.EtudiantGroupeEtudiant;
import org.esup_portail.esup_stage.repository.EtudiantGroupeEtudiantRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@ApiController
@RequestMapping("/etudiantGroupeEtudiant")
public class EtudiantGroupeEtudiantController {

    @Autowired
    EtudiantGroupeEtudiantRepository etudiantGroupeEtudiantRepository;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.CREATION_EN_MASSE_CONVENTION}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<EtudiantGroupeEtudiant> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<EtudiantGroupeEtudiant> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(etudiantGroupeEtudiantRepository.count(filters));
        paginatedResponse.setData(etudiantGroupeEtudiantRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

}