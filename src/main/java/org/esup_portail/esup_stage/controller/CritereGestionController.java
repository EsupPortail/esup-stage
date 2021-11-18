package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.model.CritereGestion;
import org.esup_portail.esup_stage.repository.CritereGestionRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/critere-gestion")
public class CritereGestionController {

    @Autowired
    CritereGestionRepository critereGestionRepository;

    @GetMapping("/centre-etapes-paginated")
    @Secure()
    public PaginatedResponse<CritereGestion> getCentreEtapesPaginated(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<CritereGestion> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(critereGestionRepository.count(filters));
        paginatedResponse.setData(critereGestionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));

        return paginatedResponse;
    }
}
