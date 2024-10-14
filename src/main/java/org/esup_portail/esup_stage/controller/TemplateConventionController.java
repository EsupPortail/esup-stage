package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.TemplateConventionDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.ParamConvention;
import org.esup_portail.esup_stage.model.TemplateConvention;
import org.esup_portail.esup_stage.repository.ParamConventionJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateConventionJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateConventionRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.ColorConverter;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiController
@RequestMapping("/template-convention")
public class TemplateConventionController {

    @Autowired
    TemplateConventionRepository templateConventionRepository;

    @Autowired
    TemplateConventionJpaRepository templateConventionJpaRepository;

    @Autowired
    ParamConventionJpaRepository paramConventionJpaRepository;

    @Autowired
    ImpressionService impressionService;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<TemplateConvention> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        PaginatedResponse<TemplateConvention> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(templateConventionRepository.count(filters));
        paginatedResponse.setData(templateConventionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping(value = "/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportExcel(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        byte[] bytes = templateConventionRepository.exportExcel(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(bytes);
    }

    @GetMapping(value = "/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        StringBuilder csv = templateConventionRepository.exportCsv(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(csv.toString());
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.CREATION})
    public TemplateConvention create(@RequestBody TemplateConvention templateConvention) {
        if (templateConventionJpaRepository.findByTypeAndLangue(templateConvention.getTypeConvention().getId(), templateConvention.getLangueConvention().getCode()) != null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Un template avec ce type et cette langue existe déjà");
        }

        TemplateConventionDto templateConventionDto = new TemplateConventionDto(ColorConverter.convertHslToRgb(templateConvention.getTexte()), ColorConverter.convertHslToRgb(templateConvention.getTexteAvenant()));
        checkTemplateConvention(templateConventionDto);

        templateConvention = templateConventionJpaRepository.saveAndFlush(templateConvention);
        return templateConvention;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public TemplateConvention update(@PathVariable("id") int id, @Valid @RequestBody TemplateConventionDto templateConventionDto) {
        checkTemplateConvention(templateConventionDto);

        TemplateConvention templateConvention = templateConventionJpaRepository.findById(id);
        if (templateConvention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Template convention non trouvé");
        }
        String convertedHtml = ColorConverter.convertHslToRgb(templateConventionDto.getTexte());
        String convertedAvenant = ColorConverter.convertHslToRgb(templateConventionDto.getTexteAvenant());
        templateConvention.setTexte(convertedHtml);
        templateConvention.setTexteAvenant(convertedAvenant);
        templateConvention = templateConventionJpaRepository.saveAndFlush(templateConvention);
        return templateConvention;
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        templateConventionJpaRepository.deleteById(id);
        templateConventionJpaRepository.flush();
    }

    @GetMapping("/default-convention")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public String getDefaultTemplateConvention() {
        return impressionService.getDefaultText(true);
    }

    @GetMapping("/default-avenant")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public String getDefaultTemplateAvenant() {
        return impressionService.getDefaultText(false);
    }

    private void checkTemplateConvention(TemplateConventionDto templateConventionDto) {
        // récupération des champs personnalisés existants
        List<ParamConvention> champs = paramConventionJpaRepository.findAll();

        // vérification des champs personnalisés du texte
        List<String> liste = extractChamps(templateConventionDto.getTexte());
        for (String champ : liste) {
            checkChamp(champs, champ);
        }

        // vérification des champs personnalisés du texte avenant
        liste = extractChamps(templateConventionDto.getTexteAvenant());
        for (String champ : liste) {
            checkChamp(champs, champ);
        }
    }

    private List<String> extractChamps(String texte) {
        List<String> liste = new ArrayList<>();
        Pattern p = Pattern.compile("(.*?)\\$\\{(.*?)\\}.*?");
        Matcher m = p.matcher(texte);
        while (m.find()) {
            liste.add(m.group(2));
        }
        return liste;
    }

    private void checkChamp(List<ParamConvention> champs, String champ) {
        if (champs.stream().noneMatch(c -> c.getCode().equals(champ.split("\\?")[0]))) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Le champ personnalisé ${" + champ + "} n'existe pas");
        }
    }
}
