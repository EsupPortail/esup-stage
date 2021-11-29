package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ContextDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
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
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    ConfidentialiteJpaRepository confidentialiteJpaRepository;

    @Autowired
    PersonnelCentreGestionJpaRepository personnelCentreGestionJpaRepository;

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
            ContextDto contexteDto = ServiceContext.getServiceContext();
            Utilisateur currentUser = contexteDto.getUtilisateur();
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

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public CentreGestion getById(@PathVariable("id") int id) {
        CentreGestion centreGestion = centreGestionJpaRepository.findById(id);
        if (centreGestion == null) {
            centreGestion = new CentreGestion();
        }
        return centreGestion;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.CREATION})
    public CentreGestion create(@Valid @RequestBody CentreGestion centreGestion) {
        CentreGestion etablissement = centreGestionJpaRepository.getCentreEtablissement();

        centreGestion.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());
        if (etablissement != null) {
            centreGestion.setCodeConfidentialite(etablissement.getCodeConfidentialite());
        }
        return centreGestionJpaRepository.saveAndFlush(centreGestion);
    }

    @PutMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public CentreGestion update(@Valid @RequestBody CentreGestion centreGestion) {
        // Les ordres de validations doivent être 1 ou 2, et on ne peut pas avoir le même ordre
        if (
                (centreGestion.getValidationPedagogiqueOrdre() != 1 && centreGestion.getValidationPedagogiqueOrdre() != 2)
                || (centreGestion.getValidationConventionOrdre() != 1 && centreGestion.getValidationConventionOrdre() != 2)
                || (Objects.equals(centreGestion.getValidationPedagogiqueOrdre(), centreGestion.getValidationConventionOrdre()))
        ) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Les ordres de validations soivent être 1 ou 2, et être différentes pour chaque types de validation");
        }
        return centreGestionJpaRepository.saveAndFlush(centreGestion);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public CentreGestion validationCreation(@PathVariable("id") int id) {
        CentreGestion centreGestion = centreGestionJpaRepository.findById(id);
        centreGestion.setValidationCreation(true);

        return centreGestionJpaRepository.saveAndFlush(centreGestion);
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        // Suppression des critères et personnels rattachés à ce centre
        critereGestionJpaRepository.deleteCriteresByCentreId(id);
        personnelCentreGestionJpaRepository.deletePersonnelsByCentreId(id);

        centreGestionJpaRepository.deleteById(id);
        centreGestionJpaRepository.flush();
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
            if (conventionJpaRepository.countConventionRattacheUfr(critereGestion.getCentreGestion().getId(), _composante.getCode()) > 0) {
                throw new AppException(HttpStatus.FORBIDDEN, "Une convention est déjà rattachée à cette composante");
            }

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

    @GetMapping("/{id}/centre-etapes")
    @Secure()
    public List<Etape> getCentreEtapes(@PathVariable("id") int id) {
        List<CritereGestion> critereGestionsEtapes = critereGestionJpaRepository.findEtapesByCentreId(id);
        List<Etape> etapes = new ArrayList<>();

        for (CritereGestion cg : critereGestionsEtapes) {
            if (cg != null) {
                Etape etape = new Etape();
                etape.setCode(cg.getId().getCode());
                etape.setCodeVrsEtp(cg.getId().getCodeVersionEtape());
                etape.setLibelle(cg.getLibelle());
                etapes.add(etape);
            }
        }

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

    @DeleteMapping("/delete-etape/{codeEtape}/{codeVersion}")
    @Secure()
    public void deleteEtape(@PathVariable("codeEtape") String codeEtape, @PathVariable("codeVersion") String codeVersion) {
        CritereGestion critereGestion = critereGestionJpaRepository.findEtapeById(codeEtape, codeVersion);
        int idCentreGestion = critereGestion.getCentreGestion().getId();

        if (conventionJpaRepository.countConventionRattacheEtape(idCentreGestion, codeEtape, codeVersion) > 0) {
            throw new AppException(HttpStatus.FORBIDDEN, "Une convention est déjà rattachée à cette étape");
        }

        critereGestionJpaRepository.delete(critereGestion);
        critereGestionJpaRepository.flush();
    }

    @GetMapping("/confidentialite")
    @Secure()
    public List<Confidentialite> getConfidentialites() {
        return confidentialiteJpaRepository.findAll();
    }

    @GetMapping("etablissement-confidentialite")
    @Secure()
    public Confidentialite getEtablissementConfidentialite() {
        CentreGestion centreGestion = centreGestionJpaRepository.getCentreEtablissement();
        return centreGestion.getCodeConfidentialite();
    }
}
