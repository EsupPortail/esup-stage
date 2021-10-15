package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.model.NafN1;
import fr.dauphine.estage.repository.NafN1Repository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

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
