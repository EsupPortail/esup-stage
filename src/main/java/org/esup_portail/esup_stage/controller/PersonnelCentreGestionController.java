package org.esup_portail.esup_stage.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.esup_portail.esup_stage.dto.LdapSearchDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.PersonnelCentreGestion;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.HabilitationService;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    UtilisateurJpaRepository utilisateurJpaRepository;

    @Autowired
    RoleJpaRepository roleJpaRepository;

    @Autowired
    AppConfigService appConfigService;

    @Autowired
    LdapService ldapService;

    @Autowired
    HabilitationService habilitationService;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<PersonnelCentreGestion> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        filters = applyCentreScope(filters, DroitEnum.LECTURE);
        PaginatedResponse<PersonnelCentreGestion> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(personnelCentreGestionRepository.count(filters));
        List<PersonnelCentreGestion> data = personnelCentreGestionRepository.findPaginated(page, perPage, predicate, sortOrder, filters);
        enrichRoles(data);
        paginatedResponse.setData(data);
        return paginatedResponse;
    }

    @GetMapping(value = "/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportExcel(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        byte[] bytes = personnelCentreGestionRepository.exportExcel(headers, predicate, sortOrder, applyCentreScope(filters, DroitEnum.LECTURE));
        return ResponseEntity.ok().body(bytes);
    }

    @GetMapping(value = "/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        StringBuilder csv = personnelCentreGestionRepository.exportCsv(headers, predicate, sortOrder, applyCentreScope(filters, DroitEnum.LECTURE));
        return ResponseEntity.ok().body(csv.toString());
    }

    @PostMapping("/{idCentre}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public PersonnelCentreGestion create(@PathVariable("idCentre") int idCentre, @Valid @RequestBody PersonnelCentreGestion personnelCentreGestion) {
        assertCanAccessCentre(idCentre, DroitEnum.MODIFICATION);
        CentreGestion centreGestion = centreGestionJpaRepository.findById(idCentre);
        List<PersonnelCentreGestion> personnels = centreGestion.getPersonnels();

        if (personnels.stream().anyMatch(p -> p.getUidPersonnel().equalsIgnoreCase(personnelCentreGestion.getUidPersonnel()))) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Ce gestionnaire est deja rattache a ce centre");
        }

        Utilisateur utilisateur = utilisateurJpaRepository.findOneByUid(personnelCentreGestion.getUidPersonnel());

        if (utilisateur == null) {
            LdapSearchDto ldapSearchDto = new LdapSearchDto();
            ldapSearchDto.setId(personnelCentreGestion.getUidPersonnel());
            List<LdapUser> ldapUsers = ldapService.search("/staff", ldapSearchDto);
            if (ldapUsers.size() == 0) {
                throw new AppException(HttpStatus.NOT_FOUND, "Gestionnaire non trouve");
            }
            utilisateur = new Utilisateur();
            utilisateur.setLogin(ldapUsers.get(0).getSupannAliasLogin());
            utilisateur.setUid(personnelCentreGestion.getUidPersonnel());
            utilisateur.setNom(personnelCentreGestion.getNom());
            utilisateur.setPrenom(personnelCentreGestion.getPrenom());
            utilisateur.setActif(true);
            utilisateur.setRoles(new ArrayList<>());
            utilisateur = utilisateurJpaRepository.saveAndFlush(utilisateur);
        }

        List<Role> centreRoles = resolveRequestedCentreRoles(personnelCentreGestion.getRoles());
        habilitationService.replaceCentreRoles(utilisateur, idCentre, centreRoles);

        personnelCentreGestion.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());
        personnelCentreGestion.setCodeUniversiteAffectation(appConfigService.getConfigGenerale().getCodeUniversite());
        personnelCentreGestion.setCentreGestion(centreGestion);
        PersonnelCentreGestion saved = personnelCentreGestionJpaRepository.saveAndFlush(personnelCentreGestion);
        saved.setRoles(centreRoles);
        return saved;
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public PersonnelCentreGestion update(@PathVariable("id") int id, @Valid @RequestBody PersonnelCentreGestion _personnelCentreGestion) {
        PersonnelCentreGestion personnelCentreGestion = personnelCentreGestionJpaRepository.findById(id);
        assertCanAccessCentre(personnelCentreGestion.getCentreGestion().getId(), DroitEnum.MODIFICATION);
        this.setPersonnelCentreData(personnelCentreGestion, _personnelCentreGestion);

        Utilisateur utilisateur = utilisateurJpaRepository.findOneByUid(personnelCentreGestion.getUidPersonnel());
        List<Role> centreRoles = resolveRequestedCentreRoles(_personnelCentreGestion.getRoles());
        if (utilisateur != null) {
            habilitationService.replaceCentreRoles(utilisateur, personnelCentreGestion.getCentreGestion().getId(), centreRoles);
        }

        PersonnelCentreGestion saved = personnelCentreGestionJpaRepository.saveAndFlush(personnelCentreGestion);
        saved.setRoles(centreRoles);
        return saved;
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION, DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        PersonnelCentreGestion personnelCentreGestion = personnelCentreGestionJpaRepository.findById(id);
        assertCanAccessCentre(personnelCentreGestion.getCentreGestion().getId(), DroitEnum.SUPPRESSION);
        Utilisateur utilisateur = utilisateurJpaRepository.findOneByUid(personnelCentreGestion.getUidPersonnel());
        if (utilisateur != null) {
            habilitationService.replaceCentreRoles(utilisateur, personnelCentreGestion.getCentreGestion().getId(), List.of());
        }
        personnelCentreGestionJpaRepository.deleteById(id);
        personnelCentreGestionJpaRepository.flush();
    }

    private void setPersonnelCentreData(PersonnelCentreGestion personnelCentreGestion, PersonnelCentreGestion requestPersonnelCentreGestion) {
        personnelCentreGestion.setTel(requestPersonnelCentreGestion.getTel());
        personnelCentreGestion.setCampus(requestPersonnelCentreGestion.getCampus());
        personnelCentreGestion.setBatiment(requestPersonnelCentreGestion.getBatiment());
        personnelCentreGestion.setBureau(requestPersonnelCentreGestion.getBureau());

        personnelCentreGestion.setImpressionConvention(requestPersonnelCentreGestion.isImpressionConvention());
        personnelCentreGestion.setDroitEvaluationEtudiant(requestPersonnelCentreGestion.getDroitEvaluationEtudiant());
        personnelCentreGestion.setDroitEvaluationEnseignant(requestPersonnelCentreGestion.getDroitEvaluationEnseignant());
        personnelCentreGestion.setDroitEvaluationEntreprise(requestPersonnelCentreGestion.getDroitEvaluationEntreprise());

        personnelCentreGestion.setAlertesMail(requestPersonnelCentreGestion.getAlertesMail());
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
        personnelCentreGestion.setVerificationAdministrativeConvention(requestPersonnelCentreGestion.getVerificationAdministrativeConvention());
        personnelCentreGestion.setValidationAvenant(requestPersonnelCentreGestion.getValidationAvenant());
        personnelCentreGestion.setConventionSignee(requestPersonnelCentreGestion.getConventionSignee());
    }

    private String applyCentreScope(String filters, DroitEnum droit) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (hasGlobalRight(utilisateur, droit)) {
            return filters;
        }
        List<Integer> centreIds = habilitationService.getAuthorizedCentreIds(utilisateur, new AppFonctionEnum[]{AppFonctionEnum.PARAM_CENTRE}, new DroitEnum[]{droit});
        JSONObject jsonFilters = filters == null || filters.isBlank() ? new JSONObject() : new JSONObject(filters);
        JSONObject filter = new JSONObject();
        filter.put("specific", true);
        filter.put("type", "list");
        filter.put("value", new JSONArray(centreIds));
        jsonFilters.put("centreIds", filter);
        return jsonFilters.toString();
    }

    private void assertCanAccessCentre(int idCentreGestion, DroitEnum droit) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (hasGlobalRight(utilisateur, droit)) {
            return;
        }
        try {
            if (habilitationService.hasCentreRight(utilisateur, idCentreGestion, new AppFonctionEnum[]{AppFonctionEnum.PARAM_CENTRE}, new DroitEnum[]{droit})) {
                return;
            }
        } catch (Exception e) {
            throw new AppException(HttpStatus.FORBIDDEN, "Vous n'avez pas acces a ce centre de gestion");
        }
        throw new AppException(HttpStatus.FORBIDDEN, "Vous n'avez pas acces a ce centre de gestion");
    }

    private boolean hasGlobalRight(Utilisateur utilisateur, DroitEnum droit) {
        try {
            return habilitationService.hasGlobalRight(utilisateur, new AppFonctionEnum[]{AppFonctionEnum.PARAM_CENTRE}, new DroitEnum[]{droit});
        } catch (Exception e) {
            return false;
        }
    }

    private List<Role> resolveRequestedCentreRoles(List<Role> requestedRoles) {
        List<Role> roles = new ArrayList<>();
        if (requestedRoles != null) {
            for (Role role : requestedRoles) {
                Role dbRole = role.getId() != 0 ? roleJpaRepository.findById(role.getId()) : roleJpaRepository.findOneByCode(role.getCode());
                if (dbRole != null && roles.stream().noneMatch(r -> r.getId() == dbRole.getId())) {
                    roles.add(dbRole);
                }
            }
        }
        if (roles.isEmpty()) {
            roles.add(roleJpaRepository.findOneByCode(Role.GES));
        }
        return roles;
    }

    private void enrichRoles(List<PersonnelCentreGestion> personnels) {
        for (PersonnelCentreGestion personnel : personnels) {
            if (personnel.getCentreGestion() != null) {
                personnel.setRoles(habilitationService.getRolesByUidAndCentre(personnel.getUidPersonnel(), personnel.getCentreGestion().getId()));
            }
        }
    }
}
