package org.esup_portail.esup_stage.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.UniteGratification;
import org.esup_portail.esup_stage.repository.ContenuJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.UniteGratificationJpaRepository;
import org.esup_portail.esup_stage.repository.UniteGratificationRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ApiController
@RequestMapping("/unite-gratification")
public class UniteGratificationController {

    @Autowired
    UniteGratificationRepository uniteGratificationRepository;

    @Autowired
    UniteGratificationJpaRepository uniteGratificationJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    ContenuJpaRepository contenuJpaRepository;

    @GetMapping
    @Secure
    public PaginatedResponse<UniteGratification> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<UniteGratification> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(uniteGratificationRepository.count(filters));
        paginatedResponse.setData(uniteGratificationRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping(value = "/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportExcel(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        byte[] bytes = uniteGratificationRepository.exportExcel(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(bytes);
    }

    @GetMapping(value = "/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        StringBuilder csv = uniteGratificationRepository.exportCsv(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(csv.toString());
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.CREATION})
    public UniteGratification create(@RequestBody UniteGratification uniteGratification) {
        if (uniteGratificationRepository.exists(uniteGratification.getLibelle(), uniteGratification.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, contenuJpaRepository.findByCode("NOMENCLATURE_LIBELLE_EXISTANT").getTexte());
        }
        uniteGratification.setTemEnServ("O");
        uniteGratification = uniteGratificationJpaRepository.saveAndFlush(uniteGratification);
        return uniteGratification;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public UniteGratification update(@PathVariable("id") int id, @RequestBody UniteGratification requestUniteGratification) {
        UniteGratification uniteGratification = uniteGratificationJpaRepository.findById(id);

        uniteGratification.setLibelle(requestUniteGratification.getLibelle());
        if (requestUniteGratification.getTemEnServ() != null) {
            uniteGratification.setTemEnServ(requestUniteGratification.getTemEnServ());
        }
        uniteGratification = uniteGratificationJpaRepository.saveAndFlush(uniteGratification);
        return uniteGratification;
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Long count = conventionJpaRepository.countConventionWithUniteGratification(id);
        if (count > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Des conventions ont déjà été créées avec ce libellé, vous ne pouvez pas le supprimer");
        }
        uniteGratificationJpaRepository.deleteById(id);
        uniteGratificationJpaRepository.flush();
    }
}
