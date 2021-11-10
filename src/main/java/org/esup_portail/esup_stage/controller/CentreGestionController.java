package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ContextDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.CentreGestionRepository;
import org.esup_portail.esup_stage.repository.CritereGestionJpaRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.Composante;
import org.esup_portail.esup_stage.service.apogee.model.Etape;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApiController
@RequestMapping("/centre-gestion")
public class CentreGestionController {

    @Autowired
    CentreGestionRepository centreGestionRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    CritereGestionJpaRepository critereGestionJpaRepository;

    @Autowired
    UtilisateurController utilisateurController;

    @Autowired
    AppConfigService appConfigService;

    @Autowired
    ApogeeService apogeeService;

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

    @GetMapping("/{id}/composantes")
    @Secure()
    public List<Composante> getComposantes(@PathVariable("id") int id) {
        List<Composante> composantes = apogeeService.getListComposante();
        List<CritereGestion> critereGestionsComposantes = critereGestionJpaRepository.findComposantes();

        composantes = composantes.stream().filter(c -> critereGestionsComposantes.stream().noneMatch(cg -> cg.getId().getCode().equalsIgnoreCase(c.getCode()) && cg.getCentreGestion().getId() != id)).collect(Collectors.toList());
        composantes.sort(Comparator.comparing(Composante::getCode));
        return composantes;
    }

    @GetMapping("/{id}/composante")
    @Secure()
    public Composante getCentreComposante(@PathVariable("id") int id) {
        CritereGestion critereGestion = critereGestionJpaRepository.findByCentreId(id);
        Composante composante = new Composante();
        if (critereGestion != null) {
            composante.setCode(critereGestion.getId().getCode());
            composante.setLibelle(critereGestion.getLibelle());
        }
        return composante;
    }

    @PutMapping("/{id}/set-composante")
    @Secure()
    public Composante setComposante(@PathVariable("id") int id, @RequestBody Composante _composante) {
        CentreGestion centreGestion = centreGestionJpaRepository.findById(id);
        CritereGestion critereGestion = critereGestionJpaRepository.findByCentreId(id);
        CritereGestionId critereGestionId = new CritereGestionId();
        critereGestionId.setCode(_composante.getCode());
        critereGestionId.setCodeVersionEtape("");

        if (critereGestion != null) {
            //todo : check convention rattachée à la composante

            critereGestionJpaRepository.delete(critereGestion);
        }

        critereGestion = new CritereGestion();
        critereGestion.setId(critereGestionId);
        critereGestion.setLibelle(_composante.getLibelle());
        critereGestion.setCentreGestion(centreGestion);
        critereGestionJpaRepository.saveAndFlush(critereGestion);
        return _composante;
    }

    @GetMapping("/{id}/etapes")
    @Secure()
    public List<Etape> getEtapes(@PathVariable("id") int id) {
        List<Etape> etapes = apogeeService.getListEtape();
        List<CritereGestion> critereGestionsEtapes = critereGestionJpaRepository.findEtapes();
        etapes = etapes.stream().filter(e -> critereGestionsEtapes.stream().noneMatch(cg -> cg.getId().getCode().equalsIgnoreCase(e.getCode()) && cg.getCentreGestion().getId() != id)).collect(Collectors.toList());
        etapes.sort(Comparator.comparing(Etape::getCodeVrsEtp).thenComparing(Etape::getCode));
        return etapes;
    }

    @PostMapping("/{id}/add-etape")
    @Secure()
    public Etape addEtape(@PathVariable("id") int id, @RequestBody Etape _etape) {
        CentreGestion centreGestion = centreGestionJpaRepository.findById(id);
        CritereGestionId critereGestionId = new CritereGestionId();
        critereGestionId.setCode(_etape.getCode());
        critereGestionId.setCodeVersionEtape(_etape.getCodeVrsEtp());

        CritereGestion critereGestion = new CritereGestion();
        critereGestion.setId(critereGestionId);
        critereGestion.setLibelle(_etape.getLibelle());
        critereGestion.setCentreGestion(centreGestion);
        critereGestionJpaRepository.saveAndFlush(critereGestion);

        return _etape;
    }

    @DeleteMapping("/delete-etape")
    @Secure()
    public void deleteEtape(@RequestBody Etape _etape) {
        CritereGestion critereGestion = critereGestionJpaRepository.findEtapeById(_etape.getCode(), _etape.getCodeVrsEtp());

        //todo : check convention rattachée à l'étape

        critereGestionJpaRepository.delete(critereGestion);
        critereGestionJpaRepository.flush();
    }

    @GetMapping("/by-etape/{codeEtape}/{codeVersion}")
    @Secure()
    public CentreGestion getByEtape(@PathVariable("codeEtape") String codeEtape, @PathVariable("codeVersion") String codeVersion) {
        CentreGestion centreGestion = centreGestionJpaRepository.findByCodeEtape(codeEtape, codeVersion);
        if (centreGestion == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Centre de gestion non trouvé");
        }
        return centreGestion;
    }
}
