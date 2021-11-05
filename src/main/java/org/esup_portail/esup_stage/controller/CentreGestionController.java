package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ContextDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.PersonnelCentreGestion;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.CentreGestionRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@ApiController
@RequestMapping("/centre-gestion")
public class CentreGestionController {

    @Autowired
    CentreGestionRepository centreGestionRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    UtilisateurController utilisateurController;

    @Autowired
    AppConfigService appConfigService;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<CentreGestion> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        JSONObject jsonFilters = new JSONObject(filters);
        Map<String, Object> map = new HashMap<>();
        map.put("type", "boolean");
        map.put("value", true);
        jsonFilters.put("validationCreation", map);
        filters = jsonFilters.toString();
        PaginatedResponse<CentreGestion> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(centreGestionRepository.count(filters));
        paginatedResponse.setData(centreGestionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));

        if (predicate.equals("personnels")) {
            Utilisateur currentUser = utilisateurController.getUserConnected();
            List<CentreGestion> list =  paginatedResponse.getData();
            Predicate<PersonnelCentreGestion> condition = value -> value.getUidPersonnel().equals(currentUser.getLogin());
            list.sort((a, b) -> Boolean.compare(a.getPersonnels().stream().anyMatch(condition), b.getPersonnels().stream().anyMatch(condition)));

            if (sortOrder.equals("asc"))
                Collections.reverse(list);
        }

        return paginatedResponse;
    }

    @GetMapping("/creation-brouillon")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.CREATION})
    public CentreGestion getBrouillonByLogin() {
        ContextDto contexteDto = ServiceContext.getServiceContext();
        Utilisateur utilisateur = contexteDto.getUtilisateur();
        CentreGestion centreGestion = centreGestionJpaRepository.findBrouillon(utilisateur.getLogin());
        if (centreGestion == null) {
            centreGestion = new CentreGestion();
        }

        return centreGestion;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.CREATION})
    public CentreGestion create(@Valid @RequestBody CentreGestion centreGestion) {
        centreGestion.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());
        return centreGestionJpaRepository.saveAndFlush(centreGestion);
    }

    @PutMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public CentreGestion update(@Valid @RequestBody CentreGestion centreGestion) {
        return centreGestionJpaRepository.saveAndFlush(centreGestion);
    }
}
