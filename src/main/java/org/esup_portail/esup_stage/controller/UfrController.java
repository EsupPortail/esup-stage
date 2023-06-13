package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.model.Ufr;
import org.esup_portail.esup_stage.repository.UfrRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.Composante;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@ApiController
@RequestMapping("/ufrs")
public class UfrController {

    @Autowired
    UfrRepository ufrRepository;

    @Autowired
    ApogeeService apogeeService;

    @GetMapping
    @Secure()
    public PaginatedResponse<Ufr> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        PaginatedResponse<Ufr> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(ufrRepository.count(filters));
        paginatedResponse.setData(ufrRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/apogee")
    @Secure()
    public List<Composante> getApogeeComposantes() {
        return apogeeService.getListComposante();
    }
}
