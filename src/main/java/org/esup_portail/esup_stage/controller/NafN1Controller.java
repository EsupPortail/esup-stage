package org.esup_portail.esup_stage.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.model.NafN1;
import org.esup_portail.esup_stage.repository.NafN1Repository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@ApiController
@RequestMapping("/nafn1")
public class NafN1Controller {

    @Autowired
    NafN1Repository nafN1Repository;

    @GetMapping
    @Secure()
    public PaginatedResponse<NafN1> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<NafN1> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(nafN1Repository.count(filters));
        paginatedResponse.setData(nafN1Repository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }
}
