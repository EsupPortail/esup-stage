package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.model.Etape;
import org.esup_portail.esup_stage.repository.EtapeRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.EtapeApogee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiController
@RequestMapping("/etapes")
public class EtapeController {

    @Autowired
    EtapeRepository etapeRepository;

    @Autowired
    ApogeeService apogeeService;

    @GetMapping
    @Secure()
    public PaginatedResponse<Etape> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        PaginatedResponse<Etape> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(etapeRepository.count(filters));
        paginatedResponse.setData(etapeRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/apogee")
    @Secure()
    public List<EtapeApogee> getApogeeEtapes() {
        return apogeeService.getListEtape();
    }

}
