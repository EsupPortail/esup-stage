package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ArchivageProgressionDto;
import org.esup_portail.esup_stage.dto.ArchivageStatistiquesDto;
import org.esup_portail.esup_stage.dto.ConventionListDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.SimulationArchivageResumeDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.repository.ConventionRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.archivage.ArchivageService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ApiController
@RequestMapping("/archivage")
public class ArchivageController {

    @Autowired
    private ArchivageService archivageService;

    @Autowired
    private ConventionRepository conventionRepository;

    @GetMapping("/statistiques")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ArchivageStatistiquesDto getStatistiques() {
        return archivageService.getStatistiques();
    }

    @GetMapping("/simulation/resume")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public SimulationArchivageResumeDto getSimulationResume() {
        return archivageService.getSimulationResume();
    }

    @PostMapping("/executer/{type}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public ArchivageProgressionDto executer(@PathVariable("type") String type) {
        if (!"archivage".equalsIgnoreCase(type) && !"purge".equalsIgnoreCase(type)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Type de traitement inconnu : " + type);
        }
        archivageService.demarrerTacheManuelle(type);
        return archivageService.getProgression();
    }

    @GetMapping("/progression")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ArchivageProgressionDto getProgression() {
        return archivageService.getProgression();
    }

    @PostMapping("/annuler")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public ArchivageProgressionDto annuler() {
        archivageService.demanderAnnulation();
        return archivageService.getProgression();
    }

    @GetMapping(value = "/rapport/export/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportRapportExcel() {
        return ResponseEntity.ok().body(archivageService.exportRapportExcel());
    }

    @GetMapping("/simulation")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<ConventionListDto> getSimulation(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        filters = addSeuilsSimulation(filters);
        PaginatedResponse<ConventionListDto> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(conventionRepository.count(filters));
        paginatedResponse.setData(conventionRepository.findPaginated(page, perPage, predicate, sortOrder, filters)
                .stream()
                .map(ConventionListDto::from)
                .toList());
        return paginatedResponse;
    }

    @GetMapping(value = "/simulation/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportSimulationExcel(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        byte[] bytes = conventionRepository.exportExcel(headers, predicate, sortOrder, addSeuilsSimulation(filters));
        return ResponseEntity.ok().body(bytes);
    }

    @GetMapping(value = "/simulation/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportSimulationCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        StringBuilder csv = conventionRepository.exportCsv(headers, predicate, sortOrder, addSeuilsSimulation(filters));
        return ResponseEntity.ok().body(csv.toString());
    }

    /**
     * Complète le filtre "simulation" envoyé par le tableau (valeur "archivage" ou "purge")
     * avec les dates seuils calculées depuis la configuration : le repository n'a pas accès
     * à la configuration, les seuils sont donc transportés dans le filtre.
     */
    private String addSeuilsSimulation(String filters) {
        JSONObject jsonFilters = new JSONObject(filters == null || filters.isBlank() ? "{}" : filters);
        String type = "archivage";
        if (jsonFilters.has("simulation")) {
            Object value = jsonFilters.getJSONObject("simulation").opt("value");
            if (value != null && "purge".equalsIgnoreCase(String.valueOf(value))) {
                type = "purge";
            }
        }
        Date now = new Date();
        Map<String, Object> value = new HashMap<>();
        value.put("type", type);
        value.put("seuilSansGratification", archivageService.getSeuilArchivageSansGratification(now).getTime());
        value.put("seuilAvecGratification", archivageService.getSeuilArchivageAvecGratification(now).getTime());
        value.put("seuilPurge", archivageService.getSeuilPurge(now).getTime());
        Map<String, Object> simulation = new HashMap<>();
        simulation.put("specific", true);
        simulation.put("value", value);
        jsonFilters.put("simulation", simulation);
        return jsonFilters.toString();
    }
}
