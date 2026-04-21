package org.esup_portail.esup_stage.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.RegimeInscriptionDto;
import org.esup_portail.esup_stage.dto.TypeConventionFormDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.RegimeInscriptionApogee;
import org.esup_portail.esup_stage.model.TypeConvention;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    RegimeInscriptionApogeeJpaRepository regimeInscriptionApogeeJpaRepository;

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
    public TypeConvention create(@RequestBody TypeConvention typeConvention) {
        if (typeConventionRepository.exists(typeConvention.getCodeCtrl(), typeConvention.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, contenuJpaRepository.findByCode("NOMENCLATURE_CODE_EXISTANT").getTexte());
        }
        typeConvention.setTemEnServ("O");
        typeConvention.setModifiable(true);
        typeConvention = typeConventionJpaRepository.saveAndFlush(typeConvention);
        return typeConvention;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public TypeConvention update(@PathVariable("id") int id, @RequestBody TypeConventionFormDto requestTypeConvention) {
        TypeConvention typeConvention = typeConventionJpaRepository.findById(id);

        typeConvention.setLibelle(requestTypeConvention.getLibelle());
        if (requestTypeConvention.getTemEnServ() != null) {
            typeConvention.setTemEnServ(requestTypeConvention.getTemEnServ());
        }
        List<RegimeInscriptionDto> regimesInscription = requestTypeConvention.getRegimesInscription();
        if (regimesInscription == null) {
            regimesInscription = requestTypeConvention.getTypeInscription();
        }
        if (regimesInscription != null) {
            typeConvention.setRegimesInscription(getRegimesInscription(regimesInscription));
        }
        typeConvention = typeConventionJpaRepository.saveAndFlush(typeConvention);
        return typeConvention;
    }

    private Set<RegimeInscriptionApogee> getRegimesInscription(List<RegimeInscriptionDto> regimesInscription) {
        Set<String> codesRegimes = regimesInscription.stream()
                .filter(regime -> regime != null && regime.getCode() != null && !regime.getCode().trim().isEmpty())
                .map(regime -> regime.getCode().trim())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<RegimeInscriptionApogee> regimes = regimeInscriptionApogeeJpaRepository.findAllById(codesRegimes);
        Set<String> codesRegimesTrouves = regimes.stream()
                .map(RegimeInscriptionApogee::getCode)
                .collect(Collectors.toSet());
        Set<String> codesRegimesInconnus = new LinkedHashSet<>(codesRegimes);
        codesRegimesInconnus.removeAll(codesRegimesTrouves);
        if (!codesRegimesInconnus.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Regime d'inscription Apogee inconnu : " + String.join(", ", codesRegimesInconnus));
        }

        return new HashSet<>(regimes);
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
}
