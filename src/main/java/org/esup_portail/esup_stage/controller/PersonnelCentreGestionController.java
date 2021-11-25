package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto;
import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto.Alerte;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiController
@RequestMapping("/personnel-centre")
public class PersonnelCentreGestionController {

    @Autowired
    PersonnelCentreGestionRepository personnelCentreGestionRepository;

    @Autowired
    PersonnelCentreGestionJpaRepository personnelCentreGestionJpaRepository;

    @Autowired
    DroitAdministrationJpaRepository droitAdministrationJpaRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    UtilisateurJpaRepository utilisateurJpaRepository;

    @Autowired
    RoleJpaRepository roleJpaRepository;

    @Autowired
    AppConfigService appConfigService;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<PersonnelCentreGestion> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<PersonnelCentreGestion> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(personnelCentreGestionRepository.count(filters));
        paginatedResponse.setData(personnelCentreGestionRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @PostMapping("/{idCentre}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.CREATION})
    public PersonnelCentreGestion create(@PathVariable("idCentre") int idCentre, @Valid @RequestBody PersonnelCentreGestion personnelCentreGestion) {
        CentreGestion centreGestion = centreGestionJpaRepository.findById(idCentre);
        Utilisateur utilisateur = utilisateurJpaRepository.findOneByLogin(personnelCentreGestion.getUidPersonnel());
        ConfigAlerteMailDto configAlerteMailDto = appConfigService.getConfigAlerteMail();
        Alerte alerte;

        if (utilisateur == null) {
            List<Role> roles = new ArrayList<>();
            roles.add(roleJpaRepository.findOneByCode(Role.GES));
            utilisateur = new Utilisateur();
            utilisateur.setLogin(personnelCentreGestion.getUidPersonnel());
            utilisateur.setNom(personnelCentreGestion.getNom());
            utilisateur.setPrenom(personnelCentreGestion.getPrenom());
            utilisateur.setActif(true);
            utilisateur.setRoles(roles);
            utilisateurJpaRepository.saveAndFlush(utilisateur);

            alerte = configAlerteMailDto.getAlerteGestionnaire();
        }
        else {
            List<Role> roles = utilisateur.getRoles();
            // si utilisateur existe en base, check s'il est GES ou resp GES
            if (roles.stream().anyMatch(r -> r.getCode().equalsIgnoreCase(Role.RESP_GES))) {
                alerte = configAlerteMailDto.getAlerteRespGestionnaire();
            }
            else if (roles.stream().anyMatch(r -> r.getCode().equalsIgnoreCase(Role.GES))) {
                alerte = configAlerteMailDto.getAlerteGestionnaire();
            }
            else {
                roles.add(roleJpaRepository.findOneByCode(Role.GES));
                utilisateur.setRoles(roles);
                utilisateurJpaRepository.saveAndFlush(utilisateur);
                alerte = configAlerteMailDto.getAlerteGestionnaire();
            }
        }

        personnelCentreGestion.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());
        personnelCentreGestion.setCodeUniversiteAffectation(appConfigService.getConfigGenerale().getCodeUniversite());
        personnelCentreGestion.setCentreGestion(centreGestion);
        this.initAlertesMail(personnelCentreGestion, alerte);
        return personnelCentreGestionJpaRepository.saveAndFlush(personnelCentreGestion);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public PersonnelCentreGestion update(@PathVariable("id") int id, @Valid @RequestBody PersonnelCentreGestion _personnelCentreGestion) {
        PersonnelCentreGestion personnelCentreGestion = personnelCentreGestionJpaRepository.findById(id);
        this.setPersonnelCentreData(personnelCentreGestion, _personnelCentreGestion);

        return personnelCentreGestionJpaRepository.saveAndFlush(personnelCentreGestion);
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        personnelCentreGestionJpaRepository.deleteById(id);
        personnelCentreGestionJpaRepository.flush();
    }

    @GetMapping("/droits-admin")
    @Secure
    public List<DroitAdministration> findAll() {
        return droitAdministrationJpaRepository.findAll();
    }

    private void setPersonnelCentreData(PersonnelCentreGestion personnelCentreGestion, PersonnelCentreGestion requestPersonnelCentreGestion) {
        personnelCentreGestion.setTel(requestPersonnelCentreGestion.getTel());
        personnelCentreGestion.setCampus(requestPersonnelCentreGestion.getCampus());
        personnelCentreGestion.setBatiment(requestPersonnelCentreGestion.getBatiment());
        personnelCentreGestion.setBureau(requestPersonnelCentreGestion.getBureau());
        personnelCentreGestion.setDroitAdministration(requestPersonnelCentreGestion.getDroitAdministration());

        personnelCentreGestion.setImpressionConvention(requestPersonnelCentreGestion.isImpressionConvention());
        personnelCentreGestion.setDroitEvaluationEtudiant(requestPersonnelCentreGestion.getDroitEvaluationEtudiant());
        personnelCentreGestion.setDroitEvaluationEnseignant(requestPersonnelCentreGestion.getDroitEvaluationEnseignant());
        personnelCentreGestion.setDroitEvaluationEntreprise(requestPersonnelCentreGestion.getDroitEvaluationEntreprise());

        personnelCentreGestion.setCreationConventionEtudiant(requestPersonnelCentreGestion.getCreationConventionEtudiant());
        personnelCentreGestion.setModificationConventionEtudiant(requestPersonnelCentreGestion.getModificationConventionEtudiant());
        personnelCentreGestion.setCreationConventionGestionnaire(requestPersonnelCentreGestion.getCreationConventionGestionnaire());
        personnelCentreGestion.setModificationConventionGestionnaire(requestPersonnelCentreGestion.getModificationConventionGestionnaire());
        personnelCentreGestion.setCreationAvenantEtudiant(requestPersonnelCentreGestion.getCreationAvenantEtudiant());
        personnelCentreGestion.setModificationAvenantEtudiant(requestPersonnelCentreGestion.getModificationAvenantEtudiant());
        personnelCentreGestion.setCreationAvenantGestionnaire(requestPersonnelCentreGestion.getCreationAvenantGestionnaire());
        personnelCentreGestion.setModificationAvenantGestionnaire(requestPersonnelCentreGestion.getModificationAvenantGestionnaire());
        personnelCentreGestion.setValidationPedagogiqueConvention(requestPersonnelCentreGestion.getValidationPedagogiqueConvention());
        personnelCentreGestion.setValidationAdministrativeConvention(requestPersonnelCentreGestion.getValidationAdministrativeConvention());
        personnelCentreGestion.setValidationAvenant(requestPersonnelCentreGestion.getValidationAvenant());
    }

    private void initAlertesMail(PersonnelCentreGestion personnelCentreGestion, Alerte alerte) {
        personnelCentreGestion.setCreationConventionEtudiant(alerte.isCreationConventionEtudiant());
        personnelCentreGestion.setModificationConventionEtudiant(alerte.isModificationConventionEtudiant());
        personnelCentreGestion.setCreationConventionGestionnaire(alerte.isCreationConventionGestionnaire());
        personnelCentreGestion.setModificationConventionGestionnaire(alerte.isModificationConventionGestionnaire());
        personnelCentreGestion.setCreationAvenantEtudiant(alerte.isCreationAvenantEtudiant());
        personnelCentreGestion.setModificationAvenantEtudiant(alerte.isModificationAvenantEtudiant());
        personnelCentreGestion.setCreationAvenantGestionnaire(alerte.isCreationAvenantGestionnaire());
        personnelCentreGestion.setModificationAvenantGestionnaire(alerte.isModificationAvenantGestionnaire());
        personnelCentreGestion.setValidationPedagogiqueConvention(alerte.isValidationPedagogiqueConvention());
        personnelCentreGestion.setValidationAdministrativeConvention(alerte.isValidationAdministrativeConvention());
        personnelCentreGestion.setValidationAvenant(alerte.isValidationAvenant());
    }
}
