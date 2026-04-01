package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.model.Etape;
import org.esup_portail.esup_stage.repository.EtapeRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.DiplomeEtape;
import org.esup_portail.esup_stage.service.apogee.model.EtapeV2Apogee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;

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
    public DiplomeEtape[] getApogeeEtapes(@RequestParam(name = "codeComposante") String codeComposante, @RequestParam(name = "codeAnnee") String codeAnnee) {
        DiplomeEtape[] listDiplomeEtape = apogeeService.getListDiplomeEtape(codeComposante, codeAnnee);
        Collator collator = Collator.getInstance();
        Arrays.sort(listDiplomeEtape, Comparator.comparing(DiplomeEtape::getLibDiplome, collator));
        for (DiplomeEtape diplomeEtape : listDiplomeEtape)
            diplomeEtape.getListeEtapes().sort(Comparator.comparing(EtapeV2Apogee::getLibWebVet,collator));
        return listDiplomeEtape;
    }

}
