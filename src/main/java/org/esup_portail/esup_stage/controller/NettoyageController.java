package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ArchivageProgressionDto;
import org.esup_portail.esup_stage.dto.NettoyageContactDto;
import org.esup_portail.esup_stage.dto.NettoyageResumeDto;
import org.esup_portail.esup_stage.dto.NettoyageServiceDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.repository.ContactRepository;
import org.esup_portail.esup_stage.repository.ServiceRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.nettoyage.NettoyageService;
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

/**
 * Simulation et lancement manuel du nettoyage des contacts et services d'accueil inutilisés,
 * depuis la page d'administration de l'archivage.
 */
@ApiController
@RequestMapping("/nettoyage")
public class NettoyageController {

    @Autowired
    private NettoyageService nettoyageService;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private org.esup_portail.esup_stage.service.crontask.CronTaskService cronTaskService;

    /**
     * État des tâches planifiées de nettoyage. Volontairement sans les compteurs d'inutilisés :
     * leur dénombrement est coûteux et n'est demandé qu'à l'ouverture de l'onglet concerné
     * (voir {@link #getNombreInutilises(String)}).
     */
    @GetMapping("/resume")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public NettoyageResumeDto getResume() {
        NettoyageResumeDto dto = new NettoyageResumeDto();
        dto.setTacheContacts(cronTaskService.getByNom("SupprimerContactsInutilises"));
        dto.setTacheServices(cronTaskService.getByNom("SupprimerServicesInutilises"));
        dto.setProchaineExecutionContacts(cronTaskService.getProchaineExecution(dto.getTacheContacts()));
        dto.setProchaineExecutionServices(cronTaskService.getProchaineExecution(dto.getTacheServices()));
        return dto;
    }

    /**
     * Nombre de contacts (ou services) inutilisés. Requête coûteuse : résultat mis en cache
     * quelques minutes côté service, et invalidé après chaque nettoyage.
     */
    @GetMapping("/nombre/{type}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public long getNombreInutilises(@PathVariable("type") String type) {
        if ("services".equalsIgnoreCase(type)) {
            return nettoyageService.compterServicesInutilises();
        }
        if ("contacts".equalsIgnoreCase(type)) {
            return nettoyageService.compterContactsInutilises();
        }
        throw new AppException(HttpStatus.BAD_REQUEST, "Type de nettoyage inconnu : " + type);
    }

    @GetMapping("/simulation/contacts")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<NettoyageContactDto> getContactsInutilises(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        String f = withInutilise(filters);
        PaginatedResponse<NettoyageContactDto> response = new PaginatedResponse<>();
        // Sans filtre de recherche, le total est celui du compteur déjà mis en cache : évite de
        // rejouer le dénombrement coûteux à chaque ouverture d'onglet ou changement de page
        response.setTotal(sansFiltreDeRecherche(filters) ? nettoyageService.compterContactsInutilises() : contactRepository.count(f));
        response.setData(contactRepository.findPaginated(page, perPage, predicate, sortOrder, f).stream().map(NettoyageContactDto::from).toList());
        return response;
    }

    @GetMapping(value = "/simulation/contacts/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportContactsInutilises(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        return ResponseEntity.ok().body(contactRepository.exportExcel(headers, predicate, sortOrder, withInutilise(filters)));
    }

    @GetMapping(value = "/simulation/contacts/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportContactsInutilisesCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        return ResponseEntity.ok().body(contactRepository.exportCsv(headers, predicate, sortOrder, withInutilise(filters)).toString());
    }

    @GetMapping("/simulation/services")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<NettoyageServiceDto> getServicesInutilises(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        String f = withInutilise(filters);
        PaginatedResponse<NettoyageServiceDto> response = new PaginatedResponse<>();
        // Voir getContactsInutilises : réutilisation du compteur en cache si aucun filtre saisi
        response.setTotal(sansFiltreDeRecherche(filters) ? nettoyageService.compterServicesInutilises() : serviceRepository.count(f));
        response.setData(serviceRepository.findPaginated(page, perPage, predicate, sortOrder, f).stream().map(NettoyageServiceDto::from).toList());
        return response;
    }

    @GetMapping(value = "/simulation/services/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportServicesInutilises(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        return ResponseEntity.ok().body(serviceRepository.exportExcel(headers, predicate, sortOrder, withInutilise(filters)));
    }

    @GetMapping(value = "/simulation/services/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportServicesInutilisesCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        return ResponseEntity.ok().body(serviceRepository.exportCsv(headers, predicate, sortOrder, withInutilise(filters)).toString());
    }

    @PostMapping("/executer/{type}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public ArchivageProgressionDto executer(@PathVariable("type") String type) {
        if (!"contacts".equalsIgnoreCase(type) && !"services".equalsIgnoreCase(type)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Type de nettoyage inconnu : " + type);
        }
        nettoyageService.demarrerNettoyageManuel(type);
        return nettoyageService.getProgression();
    }

    @GetMapping("/progression")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ArchivageProgressionDto getProgression() {
        return nettoyageService.getProgression();
    }

    @PostMapping("/annuler")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public ArchivageProgressionDto annuler() {
        nettoyageService.demanderAnnulation();
        return nettoyageService.getProgression();
    }

    @GetMapping(value = "/rapport/export/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportRapportExcel() {
        return ResponseEntity.ok().body(nettoyageService.exportRapportExcel());
    }

    /**
     * Force le filtre « inutilise » : les tableaux et exports ne montrent que les
     * contacts/services que le nettoyage supprimerait.
     */
    /**
     * Vrai si l'utilisateur n'a saisi aucun filtre de recherche : le total affiché est alors
     * le nombre global d'inutilisés, que l'on peut lire dans le cache.
     */
    private boolean sansFiltreDeRecherche(String filters) {
        if (filters == null || filters.isBlank()) {
            return true;
        }
        return new JSONObject(filters).keySet().stream().noneMatch(cle -> !"inutilise".equals(cle));
    }

    private String withInutilise(String filters) {
        JSONObject jsonFilters = new JSONObject(filters == null || filters.isBlank() ? "{}" : filters);
        JSONObject inutilise = new JSONObject();
        inutilise.put("specific", true);
        inutilise.put("value", true);
        jsonFilters.put("inutilise", inutilise);
        return jsonFilters.toString();
    }
}
