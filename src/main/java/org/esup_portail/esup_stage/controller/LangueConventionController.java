package org.esup_portail.esup_stage.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.LangueConvention;
import org.esup_portail.esup_stage.repository.ContenuJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.LangueConventionJpaRepository;
import org.esup_portail.esup_stage.repository.LangueConventionRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ApiController
@RequestMapping("/langue-convention")
public class LangueConventionController {

    @Autowired
    LangueConventionRepository langueConventionRepository;

    @Autowired
    LangueConventionJpaRepository langueConventionJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    ContenuJpaRepository contenuJpaRepository;

    @GetMapping
    @Secure
    public PaginatedResponse<LangueConvention> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<LangueConvention> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(langueConventionRepository.count(filters));
        paginatedResponse.setData(langueConventionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping(value = "/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportExcel(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        byte[] bytes = langueConventionRepository.exportExcel(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(bytes);
    }

    @GetMapping(value = "/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        StringBuilder csv = langueConventionRepository.exportCsv(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(csv.toString());
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.CREATION})
    public LangueConvention create(@RequestBody LangueConvention langueConvention) {
        if (langueConventionRepository.exists(langueConvention)) {
            throw new AppException(HttpStatus.BAD_REQUEST, contenuJpaRepository.findByCode("NOMENCLATURE_CODE_EXISTANT").getTexte());
        }
        langueConvention.setTemEnServ("O");
        langueConvention = langueConventionJpaRepository.saveAndFlush(langueConvention);
        return langueConvention;
    }

    @PutMapping("/{code}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public LangueConvention update(@PathVariable("code") String code, @RequestBody LangueConvention requestLangueConvention) {
        LangueConvention langueConvention = langueConventionJpaRepository.findByCode(code);

        langueConvention.setLibelle(requestLangueConvention.getLibelle());
        if (requestLangueConvention.getTemEnServ() != null) {
            langueConvention.setTemEnServ(requestLangueConvention.getTemEnServ());
        }
        langueConvention = langueConventionJpaRepository.saveAndFlush(langueConvention);
        return langueConvention;
    }

    @DeleteMapping("/{code}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("code") String code) {
        LangueConvention langueConvention = langueConventionJpaRepository.findByCode(code);
        Long count = conventionJpaRepository.countConventionWithLangueConvention(code);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        langueConventionJpaRepository.delete(langueConvention);
        langueConventionJpaRepository.flush();
    }
}
