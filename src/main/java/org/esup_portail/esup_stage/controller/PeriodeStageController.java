package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.PeriodeStageDto;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.PeriodeStage;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.PeriodeStageJpaRepository;
import org.esup_portail.esup_stage.repository.PeriodeStageRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiController
@RequestMapping("/periode-stage")
public class PeriodeStageController {

    @Autowired
    PeriodeStageJpaRepository periodeStageJpaRepository;

    @Autowired
    PeriodeStageRepository periodeStageRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @JsonView(Views.List.class)
    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<PeriodeStage> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        PaginatedResponse<PeriodeStage> paginatedResponse = new PaginatedResponse<>();
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            JSONObject jsonFilters = new JSONObject(filters);
            Map<String, Object> currentUser = new HashMap<>();
            currentUser.put("type", "text");
            currentUser.put("value", utilisateur.getUid());
            jsonFilters.put("convention.etudiant.identEtudiant", currentUser);
            filters = jsonFilters.toString();
        }
        paginatedResponse.setTotal(periodeStageRepository.count(filters));
        paginatedResponse.setData(periodeStageRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @JsonView(Views.List.class)
    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public PeriodeStage getById(@PathVariable("id") int id) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        PeriodeStage periodeStage = periodeStageJpaRepository.findById(id);
        if (periodeStage == null || (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(periodeStage.getConvention().getEtudiant().getIdentEtudiant()))) {
            throw new AppException(HttpStatus.NOT_FOUND, "PeriodeStage non trouvée");
        }
        return periodeStage;
    }

    @JsonView(Views.List.class)
    @GetMapping("/getByConvention/{id}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public List<PeriodeStage> getByConvention(@PathVariable("id") int id) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null || (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(convention.getEtudiant().getIdentEtudiant()))) {
            return new ArrayList<>();
        }
        return periodeStageJpaRepository.findByConvention(convention);
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.CREATION})
    public PeriodeStage create(@Valid @RequestBody PeriodeStageDto periodeStageDto) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        Integer idConvention = periodeStageDto.getIdConvention();
        if (idConvention == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Convention ID must not be null");
        }
        Convention convention = conventionJpaRepository.findById(idConvention).orElse(null);
        if (convention == null || (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(convention.getEtudiant().getIdentEtudiant()))) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        PeriodeStage periodeStage = new PeriodeStage();
        setPeriodeStageData(periodeStage, periodeStageDto);
        return periodeStageJpaRepository.saveAndFlush(periodeStage);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public PeriodeStage update(@PathVariable("id") int id, @Valid @RequestBody PeriodeStageDto periodeStageDto) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        PeriodeStage periodeStage = periodeStageJpaRepository.findById(id);
        if (periodeStage == null || (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(periodeStage.getConvention().getEtudiant().getIdentEtudiant()))) {
            throw new AppException(HttpStatus.NOT_FOUND, "PeriodeStage non trouvée");
        }
        setPeriodeStageData(periodeStage, periodeStageDto);
        return periodeStageJpaRepository.saveAndFlush(periodeStage);
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.SUPPRESSION})
    public boolean delete(@PathVariable("id") int id) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        PeriodeStage periodeStage = periodeStageJpaRepository.findById(id);
        if (periodeStage == null || (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(periodeStage.getConvention().getEtudiant().getIdentEtudiant()))) {
            throw new AppException(HttpStatus.NOT_FOUND, "PeriodeStage non trouvée");
        }
        periodeStageJpaRepository.delete(periodeStage);
        periodeStageJpaRepository.flush();
        return true;
    }

    @DeleteMapping("/delete-by-convention/{idConvention}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.SUPPRESSION})
    public boolean deleteByConvention(@PathVariable("idConvention") int idConvention) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        Convention convention = conventionJpaRepository.findById(idConvention);
        if (convention == null || (UtilisateurHelper.isRole(utilisateur, Role.ETU) && !utilisateur.getUid().equals(convention.getEtudiant().getIdentEtudiant()))) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        periodeStageJpaRepository.deleteByConvention(idConvention);
        periodeStageJpaRepository.flush();
        return true;
    }

    private void setPeriodeStageData(PeriodeStage periodeStage, PeriodeStageDto periodeStageDto) {
        Convention convention = conventionJpaRepository.findById(periodeStageDto.getIdConvention()).orElse(null);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        periodeStage.setDateDebut(periodeStageDto.getDateDebut());
        periodeStage.setDateFin(periodeStageDto.getDateFin());
        periodeStage.setNbHeuresJournalieres(periodeStageDto.getNbHeuresJournalieres());
        periodeStage.setConvention(convention);
    }
}