package org.esup_portail.esup_stage.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.TemplateConvention;
import org.esup_portail.esup_stage.model.TypeConvention;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@ApiController
@RequestMapping("/type-convention")
public class TypeConventionController {

    @Autowired
    TypeConventionRepository typeConventionRepository;

    @Autowired
    TypeConventionJpaRepository typeConventionJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    ContenuJpaRepository contenuJpaRepository;

    @Autowired
    TemplateConventionJpaRepository templateConventionJpaRepository;

    @GetMapping
    @Secure
    public PaginatedResponse<TypeConvention> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<TypeConvention> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(typeConventionRepository.count(filters));
        paginatedResponse.setData(typeConventionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping(value = "/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportExcel(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        byte[] bytes = typeConventionRepository.exportExcel(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(bytes);
    }

    @GetMapping(value = "/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        StringBuilder csv = typeConventionRepository.exportCsv(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(csv.toString());
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.CREATION})
    public TypeConvention create(@RequestBody TypeConvention requestTypeConvention) {
        if (typeConventionRepository.exists(requestTypeConvention.getCodeCtrl(), requestTypeConvention.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, contenuJpaRepository.findByCode("NOMENCLATURE_CODE_EXISTANT").getTexte());
        }

        TypeConvention typeConvention = new TypeConvention();
        typeConvention.setLibelle(requestTypeConvention.getLibelle());
        typeConvention.setCodeCtrl(requestTypeConvention.getCodeCtrl());
        typeConvention.setTemEnServ("O");
        typeConvention.setModifiable(true);

        typeConvention = typeConventionJpaRepository.saveAndFlush(typeConvention);
        synchronizeTemplateConventions(typeConvention, requestTypeConvention.getTemplateConventions());

        return typeConvention;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public TypeConvention update(@PathVariable("id") int id, @RequestBody TypeConvention requestTypeConvention) {
        TypeConvention typeConvention = typeConventionJpaRepository.findById(id);
        if (typeConvention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Type convention non trouvé");
        }

        typeConvention.setLibelle(requestTypeConvention.getLibelle());
        if (requestTypeConvention.getTemEnServ() != null) {
            typeConvention.setTemEnServ(requestTypeConvention.getTemEnServ());
        }

        typeConvention = typeConventionJpaRepository.saveAndFlush(typeConvention);

        // Important: ne synchroniser les templates que si le champ est présent dans le payload
        synchronizeTemplateConventions(typeConvention, requestTypeConvention.getTemplateConventions());

        return typeConvention;
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long countConvention = conventionJpaRepository.countConventionWithTypeConvention(id);
        if (countConvention > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        Long countTemplateConvention = templateConventionJpaRepository.countTemplateWithTypeConvention(id);
        if (countTemplateConvention > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des templates de convention ont déjà été créés avec ce libellé, vous ne pouvez pas le supprimer");
        }
        typeConventionJpaRepository.deleteById(id);
        typeConventionJpaRepository.flush();
    }

    private void synchronizeTemplateConventions(TypeConvention typeConvention, List<TemplateConvention> requestedTemplates) {
        if (requestedTemplates == null) {
            return;
        }

        Set<Integer> selectedTemplateIds = new HashSet<>();
        for (TemplateConvention template : requestedTemplates) {
            if (template != null && template.getId() > 0) {
                selectedTemplateIds.add(template.getId());
            }
        }

        List<TemplateConvention> allTemplates = templateConventionJpaRepository.findAll();
        List<TemplateConvention> templatesToSave = new ArrayList<>();

        for (TemplateConvention template : allTemplates) {
            boolean shouldBeLinked = selectedTemplateIds.contains(template.getId());
            List<TypeConvention> linkedTypes = template.getTypeConventions();
            if (linkedTypes == null) {
                linkedTypes = new ArrayList<>();
            }

            boolean alreadyLinked = linkedTypes.stream()
                    .filter(Objects::nonNull)
                    .anyMatch(tc -> tc.getId() == typeConvention.getId());

            if (shouldBeLinked && !alreadyLinked) {
                linkedTypes.add(typeConvention);
                template.setTypeConventions(linkedTypes);
                templatesToSave.add(template);
            } else if (!shouldBeLinked && alreadyLinked) {
                linkedTypes.removeIf(tc -> tc != null && tc.getId() == typeConvention.getId());
                template.setTypeConventions(linkedTypes);
                templatesToSave.add(template);
            }
        }

        if (!templatesToSave.isEmpty()) {
            templateConventionJpaRepository.saveAll(templatesToSave);
            templateConventionJpaRepository.flush();
        }
    }
}