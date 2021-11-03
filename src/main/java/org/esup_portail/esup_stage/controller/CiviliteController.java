package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Civilite;
import org.esup_portail.esup_stage.repository.CiviliteJpaRepository;
import org.esup_portail.esup_stage.repository.CiviliteRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@ApiController
@RequestMapping("/civilites")
public class CiviliteController {

    @Autowired
    CiviliteRepository civiliteRepository;

    @Autowired
    CiviliteJpaRepository civiliteJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Civilite> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Civilite> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(civiliteRepository.count(filters));
        paginatedResponse.setData(civiliteRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.LECTURE})
    public Civilite getById(@PathVariable("id") int id) {
        Civilite civilite = civiliteJpaRepository.findById(id);
        if (civilite == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Civilite non trouvée");
        }
        return civilite;
    }

    @PostMapping
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.CREATION})
    public Civilite create(@Valid @RequestBody Civilite _civilite) {
        return civiliteJpaRepository.saveAndFlush(_civilite);
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.ORGA_ACC, droits = {DroitEnum.MODIFICATION})
    public Civilite update(@PathVariable("id") int id, @Valid @RequestBody Civilite _civilite) {
        Civilite civilite = civiliteJpaRepository.findById(id);
        if (civilite == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Civilite non trouvée");
        }
        civilite.setLibelle(_civilite.getLibelle());
        civilite = civiliteJpaRepository.saveAndFlush(civilite);
        return civilite;
    }

}
