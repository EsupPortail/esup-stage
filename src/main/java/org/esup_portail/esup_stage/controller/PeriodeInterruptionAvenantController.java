package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.PeriodeInterruptionAvenantDto;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.esup_portail.esup_stage.repository.PeriodeInterruptionAvenantJpaRepository;
import org.esup_portail.esup_stage.repository.PeriodeInterruptionAvenantRepository;
import org.esup_portail.esup_stage.repository.PeriodeInterruptionStageJpaRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiController
@RequestMapping("/periode-interruption-avenant")
public class PeriodeInterruptionAvenantController {

    @Autowired
    PeriodeInterruptionAvenantRepository periodeInterruptionAvenantRepository;

    @Autowired
    PeriodeInterruptionAvenantJpaRepository periodeInterruptionAvenantJpaRepository;

    @Autowired
    PeriodeInterruptionStageJpaRepository periodeInterruptionStageJpaRepository;

    @Autowired
    AvenantJpaRepository avenantJpaRepository;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.AVENANT}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<PeriodeInterruptionAvenant> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        Utilisateur utilisateur = ServiceContext.getServiceContext().getUtilisateur();
        PaginatedResponse<PeriodeInterruptionAvenant> paginatedResponse = new PaginatedResponse<>();
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            JSONObject jsonFilters = new JSONObject(filters);
            Map<String, Object> currentUser = new HashMap<>();
            currentUser.put("type", "text");
            currentUser.put("value", utilisateur.getUid());
            jsonFilters.put("avenant.convention.etudiant.identEtudiant", currentUser);
            filters = jsonFilters.toString();
        }
        paginatedResponse.setTotal(periodeInterruptionAvenantRepository.count(filters));
        paginatedResponse.setData(periodeInterruptionAvenantRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.AVENANT}, droits = {DroitEnum.LECTURE})
    public PeriodeInterruptionAvenant getById(@PathVariable("id") int id) {
        Utilisateur utilisateur = ServiceContext.getServiceContext().getUtilisateur();
        PeriodeInterruptionAvenant periodeInterruptionAvenant = periodeInterruptionAvenantJpaRepository.findById(id);
        if (periodeInterruptionAvenant == null || (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(periodeInterruptionAvenant.getAvenant().getConvention().getEtudiant().getIdentEtudiant()))) {
            throw new AppException(HttpStatus.NOT_FOUND, "PeriodeInterruptionAvenant non trouvée");
        }
        return periodeInterruptionAvenant;
    }

    @JsonView(Views.List.class)
    @GetMapping("/getByAvenant/{id}")
    @Secure(fonctions = {AppFonctionEnum.AVENANT}, droits = {DroitEnum.LECTURE})
    public List<PeriodeInterruptionAvenant> getByAvenant(@PathVariable("id") int id) {
        Utilisateur utilisateur = ServiceContext.getServiceContext().getUtilisateur();
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null || (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(avenant.getConvention().getEtudiant().getIdentEtudiant()))) {
            return new ArrayList<>();
        }
        return periodeInterruptionAvenantJpaRepository.findByAvenant(id);
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.AVENANT}, droits = {DroitEnum.CREATION})
    public PeriodeInterruptionAvenant create(@Valid @RequestBody PeriodeInterruptionAvenantDto periodeInterruptionAvenantDto) {
        Utilisateur utilisateur = ServiceContext.getServiceContext().getUtilisateur();
        Avenant avenant = avenantJpaRepository.findById(periodeInterruptionAvenantDto.getIdAvenant());
        if (avenant == null || (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(avenant.getConvention().getEtudiant().getIdentEtudiant()))) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvé");
        }
        PeriodeInterruptionAvenant periodeInterruptionAvenant = new PeriodeInterruptionAvenant();
        setPeriodeInterruptionAvenantData(periodeInterruptionAvenant, periodeInterruptionAvenantDto);
        return periodeInterruptionAvenantJpaRepository.saveAndFlush(periodeInterruptionAvenant);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.AVENANT}, droits = {DroitEnum.MODIFICATION})
    public PeriodeInterruptionAvenant update(@PathVariable("id") int id, @Valid @RequestBody PeriodeInterruptionAvenantDto periodeInterruptionAvenantDto) {
        Utilisateur utilisateur = ServiceContext.getServiceContext().getUtilisateur();
        PeriodeInterruptionAvenant periodeInterruptionAvenant = periodeInterruptionAvenantJpaRepository.findById(id);
        if (periodeInterruptionAvenant == null || (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(periodeInterruptionAvenant.getAvenant().getConvention().getEtudiant().getIdentEtudiant()))) {
            throw new AppException(HttpStatus.NOT_FOUND, "PeriodeInterruptionAvenant non trouvé");
        }
        setPeriodeInterruptionAvenantData(periodeInterruptionAvenant, periodeInterruptionAvenantDto);
        return periodeInterruptionAvenantJpaRepository.saveAndFlush(periodeInterruptionAvenant);
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.AVENANT}, droits = {DroitEnum.SUPPRESSION})
    public boolean delete(@PathVariable("id") int id) {
        Utilisateur utilisateur = ServiceContext.getServiceContext().getUtilisateur();
        PeriodeInterruptionAvenant periodeInterruptionAvenant = periodeInterruptionAvenantJpaRepository.findById(id);
        if (periodeInterruptionAvenant == null || (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(periodeInterruptionAvenant.getAvenant().getConvention().getEtudiant().getIdentEtudiant()))) {
            throw new AppException(HttpStatus.NOT_FOUND, "PeriodeInterruptionAvenant non trouvé");
        }
        periodeInterruptionAvenantJpaRepository.delete(periodeInterruptionAvenant);
        periodeInterruptionAvenantJpaRepository.flush();
        return true;
    }

    @DeleteMapping("/deleteAll/{id}")
    @Secure(fonctions = {AppFonctionEnum.AVENANT}, droits = {DroitEnum.SUPPRESSION})
    public boolean deleteAll(@PathVariable("id") int id) {
        Utilisateur utilisateur = ServiceContext.getServiceContext().getUtilisateur();
        Avenant avenant = avenantJpaRepository.findById(id);
        if (avenant == null || (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(avenant.getConvention().getEtudiant().getIdentEtudiant()))) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvé");
        }
        List<PeriodeInterruptionAvenant> periodeInterruptionAvenants = periodeInterruptionAvenantJpaRepository.findByAvenant(id);
        for(PeriodeInterruptionAvenant periodeInterruptionAvenant : periodeInterruptionAvenants){
            periodeInterruptionAvenantJpaRepository.delete(periodeInterruptionAvenant);
            periodeInterruptionAvenantJpaRepository.flush();
        }
        return true;
    }

    private void setPeriodeInterruptionAvenantData(PeriodeInterruptionAvenant periodeInterruptionAvenant, PeriodeInterruptionAvenantDto periodeInterruptionAvenantDto) {

        Avenant avenant = avenantJpaRepository.findById(periodeInterruptionAvenantDto.getIdAvenant());
        if (avenant == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Avenant non trouvée");
        }

        PeriodeInterruptionStage periodeInterruptionStage = periodeInterruptionStageJpaRepository.findById(periodeInterruptionAvenantDto.getIdPeriodeInterruptionStage());

        periodeInterruptionAvenant.setDateDebutInterruption(periodeInterruptionAvenantDto.getDateDebutInterruption());
        periodeInterruptionAvenant.setDateFinInterruption(periodeInterruptionAvenantDto.getDateFinInterruption());
        periodeInterruptionAvenant.setIsModif(periodeInterruptionAvenantDto.getIsModif());
        periodeInterruptionAvenant.setPeriodeInterruptionStage(periodeInterruptionStage);
        periodeInterruptionAvenant.setAvenant(avenant);
    }
}