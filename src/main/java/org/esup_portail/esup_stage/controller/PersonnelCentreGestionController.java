package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.DroitAdministration;
import org.esup_portail.esup_stage.model.PersonnelCentreGestion;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.DroitAdministrationJpaRepository;
import org.esup_portail.esup_stage.repository.PersonnelCentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.PersonnelCentreGestionRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@ApiController
@RequestMapping("/personnel-centre")
public class PersonnelCentreGestionController {

    @Autowired
    PersonnelCentreGestionRepository personnelCentreGestionRepository;

    @Autowired
    PersonnelCentreGestionJpaRepository personnelCentreGestionJpaRepository;

    @Autowired
    DroitAdministrationJpaRepository droitAdministrationJpaRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    AppConfigService appConfigService;

    @GetMapping
    @Secure
    public PaginatedResponse<PersonnelCentreGestion> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<PersonnelCentreGestion> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(personnelCentreGestionRepository.count(filters));
        paginatedResponse.setData(personnelCentreGestionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PostMapping("/{idCentre}")
    @Secure
    public PersonnelCentreGestion create(@PathVariable("idCentre") int idCentre, @Valid @RequestBody PersonnelCentreGestion personnelCentreGestion) {
        CentreGestion centreGestion = centreGestionJpaRepository.findById(idCentre);
        personnelCentreGestion.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());
        personnelCentreGestion.setCodeUniversiteAffectation(appConfigService.getConfigGenerale().getCodeUniversite());
        personnelCentreGestion.setCentreGestion(centreGestion);
        return personnelCentreGestionJpaRepository.saveAndFlush(personnelCentreGestion);
    }

    @PutMapping
    @Secure
    public PersonnelCentreGestion update(@Valid @RequestBody PersonnelCentreGestion personnelCentreGestion) {
        return personnelCentreGestionJpaRepository.saveAndFlush(personnelCentreGestion);
    }

    @GetMapping("/droits-admin")
    @Secure
    public List<DroitAdministration> findAll() {
        return droitAdministrationJpaRepository.findAll();
    }
}
