package fr.dauphine.estage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.dto.view.Views;
import fr.dauphine.estage.model.RoleEnum;
import fr.dauphine.estage.repository.ConventionRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/conventions")
public class ConventionController {

    @Autowired
    ConventionRepository conventionRepository;

    @JsonView(Views.List.class)
    @GetMapping
    @Secure(roles = {RoleEnum.ADM, RoleEnum.RESP_GES, RoleEnum.GES, RoleEnum.ENS, RoleEnum.ETU, RoleEnum.OBS})
    public PaginatedResponse search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        // TODO filtrer la liste en fonction des droits
        PaginatedResponse paginatedResponse = new PaginatedResponse();
        paginatedResponse.setTotal(conventionRepository.count(filters));
        paginatedResponse.setData(conventionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }
}
