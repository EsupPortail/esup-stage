package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.model.DroitAdministration;
import org.esup_portail.esup_stage.model.PersonnelCentreGestion;
import org.esup_portail.esup_stage.repository.DroitAdministrationJpaRepository;
import org.esup_portail.esup_stage.repository.PersonnelCentreGestionRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@ApiController
@RequestMapping("/personnel-centre")
public class PersonnelCentreGestionController {

    @Autowired
    PersonnelCentreGestionRepository personnelCentreGestionRepository;

    @Autowired
    DroitAdministrationJpaRepository droitAdministrationJpaRepository;

    @GetMapping
    @Secure
    public PaginatedResponse<PersonnelCentreGestion> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<PersonnelCentreGestion> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(personnelCentreGestionRepository.count(filters));
        paginatedResponse.setData(personnelCentreGestionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/droits-admin")
    @Secure
    public List<DroitAdministration> findAll() {
        return droitAdministrationJpaRepository.findAll();
    }
}
