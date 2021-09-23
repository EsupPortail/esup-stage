package fr.dauphine.estage.controller;

import fr.dauphine.estage.dto.PaginatedResponse;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.model.LangueConvention;
import fr.dauphine.estage.repository.LangueConventionJpaRepository;
import fr.dauphine.estage.repository.LangueConventionRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/langue-convention")
public class LangueConventionController {

    @Autowired
    LangueConventionRepository langueConventionRepository;

    @Autowired
    LangueConventionJpaRepository langueConventionJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<LangueConvention> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<LangueConvention> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(langueConventionRepository.count(filters));
        paginatedResponse.setData(langueConventionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PutMapping("/{code}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public LangueConvention update(@PathVariable("code") String code, @RequestBody LangueConvention requestLangueConvention) {
        LangueConvention langueConvention = langueConventionJpaRepository.findByCode(code);

        langueConvention.setLibelle(requestLangueConvention.getLibelle());
        if (requestLangueConvention.getTemEnServ() != null) {
            langueConvention.setTemEnServ(requestLangueConvention.getTemEnServ());
        }
        langueConvention = langueConventionJpaRepository.saveAndFlush(langueConvention);
        return langueConvention;
    }
}
