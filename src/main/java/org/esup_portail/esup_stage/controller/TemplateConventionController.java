package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.TemplateConvention;
import org.esup_portail.esup_stage.repository.TemplateConventionJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateConventionRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@ApiController
@RequestMapping("/template-convention")
public class TemplateConventionController {

    @Autowired
    TemplateConventionRepository templateConventionRepository;

    @Autowired
    TemplateConventionJpaRepository templateConventionJpaRepository;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<TemplateConvention> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        PaginatedResponse<TemplateConvention> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(templateConventionRepository.count(filters));
        paginatedResponse.setData(templateConventionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public TemplateConvention update(@PathVariable("id") int id, @Valid @RequestBody String texte) {

        //todo : vérif des params du template (similaire à template mail)

        TemplateConvention templateConvention = templateConventionJpaRepository.findById(id);

        if (templateConvention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Template convention non trouvé");
        }

        templateConvention.setTexte(texte);
        templateConvention = templateConventionJpaRepository.saveAndFlush(templateConvention);
        return templateConvention;
    }

}
