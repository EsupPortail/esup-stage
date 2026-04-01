package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletResponse;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.TemplateConventionDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.LangueConvention;
import org.esup_portail.esup_stage.model.ParamConvention;
import org.esup_portail.esup_stage.model.TemplateConvention;
import org.esup_portail.esup_stage.model.TypeConvention;
import org.esup_portail.esup_stage.repository.LangueConventionJpaRepository;
import org.esup_portail.esup_stage.repository.ParamConventionJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateConventionJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateConventionRepository;
import org.esup_portail.esup_stage.repository.TypeConventionJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.ColorConverter;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
    TypeConventionJpaRepository typeConventionJpaRepository;

    @Autowired
    LangueConventionJpaRepository langueConventionJpaRepository;

    @Autowired
    ImpressionService impressionService;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<TemplateConvention> search(@RequestParam(name = "page", defaultValue = "1") int page,
                                                        @RequestParam(name = "perPage", defaultValue = "50") int perPage,
                                                        @RequestParam("predicate") String predicate,
                                                        @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder,
                                                        @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        PaginatedResponse<TemplateConvention> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(templateConventionRepository.count(filters));
        paginatedResponse.setData(templateConventionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping(value = "/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportExcel(@RequestParam(name = "headers", defaultValue = "{}") String headers,
                                              @RequestParam("predicate") String predicate,
                                              @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder,
                                              @RequestParam(name = "filters", defaultValue = "{}") String filters,
                                              HttpServletResponse response) {
        byte[] bytes = templateConventionRepository.exportExcel(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(bytes);
    }

    @GetMapping(value = "/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers,
                                            @RequestParam("predicate") String predicate,
                                            @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder,
                                            @RequestParam(name = "filters", defaultValue = "{}") String filters,
                                            HttpServletResponse response) {
        StringBuilder csv = templateConventionRepository.exportCsv(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(csv.toString());
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.CREATION})
    public TemplateConvention create(@RequestBody TemplateConvention templateConvention) {
        TemplateConventionDto templateConventionDto = new TemplateConventionDto(
                ColorConverter.convertHslToRgb(templateConvention.getTexte()),
                ColorConverter.convertHslToRgb(templateConvention.getTexteAvenant())
        );
        templateConventionDto.setLibelle(templateConvention.getLibelle());
        checkTemplateConvention(templateConventionDto);

        templateConvention.setLibelle(templateConventionDto.getLibelle());
        templateConvention.setTexte(templateConventionDto.getTexte());
        templateConvention.setTexteAvenant(templateConventionDto.getTexteAvenant());
        applyAssociations(templateConvention, templateConvention.getLangueConvention(), templateConvention.getTypeConventions());

        return templateConventionJpaRepository.saveAndFlush(templateConvention);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public TemplateConvention update(@PathVariable("id") int id, @RequestBody JsonNode payload) {
        TemplateConventionDto dto = buildUpdateDto(payload);
        checkTemplateConvention(dto);

        TemplateConvention templateConvention = id > 0 ? templateConventionJpaRepository.findById(id).orElse(null) : null;

        if (templateConvention == null) {
            int lookupTypeConventionId = payload.path("lookupTypeConventionId").asInt(0);
            String lookupLangueConventionCode = payload.path("lookupLangueConventionCode").asText("").trim();
            if (lookupTypeConventionId > 0 && !lookupLangueConventionCode.isBlank()) {
                templateConvention = templateConventionJpaRepository.findByTypeAndLangue(lookupTypeConventionId, lookupLangueConventionCode);
            }
        }

        if (templateConvention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Template convention non trouve");
        }

        templateConvention.setLibelle(dto.getLibelle());
        templateConvention.setTexte(ColorConverter.convertHslToRgb(dto.getTexte()));
        templateConvention.setTexteAvenant(ColorConverter.convertHslToRgb(dto.getTexteAvenant()));
        applyAssociations(templateConvention, dto.getLangueConvention(), dto.getTypeConventions());

        return templateConventionJpaRepository.saveAndFlush(templateConvention);
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

    @GetMapping("/all")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public List<TemplateConvention> getAllTemplates() {
        return templateConventionJpaRepository.findAll();
    }

    private TemplateConventionDto buildUpdateDto(JsonNode payload) {
        TemplateConventionDto templateConventionDto = new TemplateConventionDto();
        templateConventionDto.setLibelle(payload.path("libelle").isMissingNode() || payload.path("libelle").isNull()
                ? null
                : payload.path("libelle").asText().trim());
        templateConventionDto.setTexte(payload.path("texte").isMissingNode() || payload.path("texte").isNull() ? null : payload.path("texte").asText());
        templateConventionDto.setTexteAvenant(payload.path("texteAvenant").isMissingNode() || payload.path("texteAvenant").isNull() ? null : payload.path("texteAvenant").asText());

        JsonNode langueNode = payload.path("langueConvention");
        if (!langueNode.isMissingNode() && !langueNode.isNull() && langueNode.hasNonNull("code")) {
            LangueConvention langueConvention = new LangueConvention();
            langueConvention.setCode(langueNode.path("code").asText());
            templateConventionDto.setLangueConvention(langueConvention);
        }

        List<TypeConvention> typeConventions = new ArrayList<>();
        JsonNode typesNode = payload.path("typeConventions");
        if (typesNode.isArray()) {
            for (JsonNode node : typesNode) {
                if (node != null && node.hasNonNull("id")) {
                    TypeConvention typeConvention = new TypeConvention();
                    typeConvention.setId(node.path("id").asInt());
                    typeConventions.add(typeConvention);
                }
            }
        }

        if (typeConventions.isEmpty()) {
            JsonNode typeNode = payload.path("typeConvention");
            if (!typeNode.isMissingNode() && !typeNode.isNull() && typeNode.hasNonNull("id")) {
                TypeConvention typeConvention = new TypeConvention();
                typeConvention.setId(typeNode.path("id").asInt());
                typeConventions.add(typeConvention);
            }
        }

        templateConventionDto.setTypeConventions(typeConventions);
        return templateConventionDto;
    }

    private void applyAssociations(TemplateConvention templateConvention,
                                   LangueConvention requestedLangue,
                                   List<TypeConvention> requestedTypes) {
        LangueConvention langueConvention = resolveLangueConvention(requestedLangue, templateConvention.getLangueConvention());
        templateConvention.setLangueConvention(langueConvention);

        List<TypeConvention> resolvedTypes = resolveTypeConventions(requestedTypes, templateConvention);
        templateConvention.setTypeConventions(resolvedTypes);
    }

    private LangueConvention resolveLangueConvention(LangueConvention requestedLangue, LangueConvention currentLangue) {
        String code = requestedLangue != null ? requestedLangue.getCode() : null;
        if ((code == null || code.isBlank()) && currentLangue != null) {
            return currentLangue;
        }
        if (code == null || code.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Langue de convention obligatoire");
        }

        LangueConvention langueConvention = langueConventionJpaRepository.findByCode(code);
        if (langueConvention == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Langue de convention introuvable");
        }
        return langueConvention;
    }

    private List<TypeConvention> resolveTypeConventions(List<TypeConvention> requestedTypes,
                                                        TemplateConvention currentTemplateConvention) {
        Set<Integer> typeIds = new LinkedHashSet<>();

        if (requestedTypes != null) {
            for (TypeConvention typeConvention : requestedTypes) {
                if (typeConvention != null && typeConvention.getId() > 0) {
                    typeIds.add(typeConvention.getId());
                }
            }
        }

        if (typeIds.isEmpty() && currentTemplateConvention.getTypeConventions() != null) {
            for (TypeConvention typeConvention : currentTemplateConvention.getTypeConventions()) {
                if (typeConvention != null && typeConvention.getId() > 0) {
                    typeIds.add(typeConvention.getId());
                }
            }
        }

        if (typeIds.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Au moins un type de convention est obligatoire");
        }

        List<TypeConvention> resolvedTypes = new ArrayList<>();
        for (Integer typeId : typeIds) {
            TypeConvention typeConvention = typeConventionJpaRepository.findById(typeId.intValue());
            if (typeConvention == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Type de convention introuvable");
            }
            resolvedTypes.add(typeConvention);
        }
        return resolvedTypes;
    }

    private void checkTemplateConvention(TemplateConventionDto templateConventionDto) {
        List<ParamConvention> champs = paramConventionJpaRepository.findAll();

        List<String> liste = extractChamps(templateConventionDto.getTexte());
        for (String champ : liste) {
            checkChamp(champs, champ);
        }

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
            throw new AppException(HttpStatus.BAD_REQUEST, "Le champ personnalise ${" + champ + "} n'existe pas");
        }
    }
}
