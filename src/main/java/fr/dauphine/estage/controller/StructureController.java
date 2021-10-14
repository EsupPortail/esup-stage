package fr.dauphine.estage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.dto.view.Views;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.Structure;
import fr.dauphine.estage.repository.StructureJpaRepository;
import fr.dauphine.estage.repository.StructureRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/structures")
public class StructureController {

    @Autowired
    StructureRepository structureRepository;

    @Autowired
    StructureJpaRepository structureJpaRepository;

    @JsonView(Views.List.class)
    @GetMapping
    @Secure
    public PaginatedResponse<Structure> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Structure> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(structureRepository.count(filters));
        paginatedResponse.setData(structureRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure
    public Structure getById(@PathVariable("id") int id) {
        Structure structure = structureJpaRepository.findById(id);
        if (structure == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Structure non trouvée");
        }
        return structure;
    }
}
