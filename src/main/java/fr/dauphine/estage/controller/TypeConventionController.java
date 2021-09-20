package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.model.RoleEnum;
import fr.dauphine.estage.repository.TypeConventionRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/type-convention")
public class TypeConventionController {

    @Autowired
    TypeConventionRepository typeConventionRepository;

    @GetMapping
    @Secure(roles = {RoleEnum.ADM_TECH, RoleEnum.ADM})
    public PaginatedResponse search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse paginatedResponse = new PaginatedResponse();
        paginatedResponse.setTotal(typeConventionRepository.count(filters));
        paginatedResponse.setData(typeConventionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }
}
