package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.OrigineStage;
import org.esup_portail.esup_stage.repository.ContenuJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.OrigineStageJpaRepository;
import org.esup_portail.esup_stage.repository.OrigineStageRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@ApiController
@RequestMapping("/origine-stage")
public class OrigineStageController {

    @Autowired
    OrigineStageRepository origineStageRepository;

    @Autowired
    OrigineStageJpaRepository origineStageJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    ContenuJpaRepository contenuJpaRepository;

    @GetMapping
    @Secure
    public PaginatedResponse<OrigineStage> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<OrigineStage> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(origineStageRepository.count(filters));
        paginatedResponse.setData(origineStageRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping(value = "/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportExcel(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        byte[] bytes = origineStageRepository.exportExcel(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(bytes);
    }

    @GetMapping(value = "/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        StringBuilder csv = origineStageRepository.exportCsv(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(csv.toString());
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.CREATION})
    public OrigineStage create(@RequestBody OrigineStage origineStage) {
        if (origineStageRepository.exists(origineStage.getLibelle(), origineStage.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, contenuJpaRepository.findByCode("NOMENCLATURE_LIBELLE_EXISTANT").getTexte());
        }
        origineStage.setTemEnServ("O");
        origineStage.setModifiable(true);
        origineStage = origineStageJpaRepository.saveAndFlush(origineStage);
        return origineStage;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public OrigineStage update(@PathVariable("id") int id, @RequestBody OrigineStage requestOrigineStage) {
        OrigineStage origineStage = origineStageJpaRepository.findById(id);

        origineStage.setLibelle(requestOrigineStage.getLibelle());
        if (requestOrigineStage.getTemEnServ() != null) {
            origineStage.setTemEnServ(requestOrigineStage.getTemEnServ());
        }
        origineStage = origineStageJpaRepository.saveAndFlush(origineStage);
        return origineStage;
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithOrigineStage(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        origineStageJpaRepository.deleteById(id);
        origineStageJpaRepository.flush();
    }
}
