package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.StructureFormDto;
import org.esup_portail.esup_stage.dto.TemplateMailFormDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.ParamMail;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.model.TemplateMail;
import org.esup_portail.esup_stage.repository.ParamMailJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateMailJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateMailRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@ApiController
@RequestMapping("/template-mails")
public class TemplateMailController {

    @Autowired
    TemplateMailRepository templateMailRepository;

    @Autowired
    TemplateMailJpaRepository templateMailJpaRepository;

    @Autowired
    ParamMailJpaRepository paramMailJpaRepository;

    @GetMapping
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<TemplateMail> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        PaginatedResponse<TemplateMail> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(templateMailRepository.count(filters));
        paginatedResponse.setData(templateMailRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public TemplateMail getById(@PathVariable("id") int id) {
        return templateMailJpaRepository.findById(id);
    }

    @PutMapping("/{id}")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.MODIFICATION})
    public TemplateMail update(@PathVariable("id") int id, @Valid @RequestBody TemplateMailFormDto templateMailFormDto) {
        TemplateMail templateMail = templateMailJpaRepository.findById(id);
        if (templateMail == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Template mail non trouvé");
        }
        templateMail.setLibelle(templateMailFormDto.getLibelle());
        templateMail.setObjet(templateMailFormDto.getObjet());
        templateMail.setTexte(templateMailFormDto.getTexte());
        templateMail = templateMailJpaRepository.saveAndFlush(templateMail);
        return templateMail;
    }

    @GetMapping("/params")
    @Secure(fonction = AppFonctionEnum.NOMENCLATURE, droits = {DroitEnum.LECTURE})
    public List<ParamMail> getParams() {
        return paramMailJpaRepository.findAll();
    }
}
