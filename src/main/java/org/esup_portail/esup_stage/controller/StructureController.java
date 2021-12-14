package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.ContextDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.StructureFormDto;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Date;

@ApiController
@RequestMapping("/structures")
public class StructureController {

    @Autowired
    StructureRepository structureRepository;

    @Autowired
    StructureJpaRepository structureJpaRepository;

    @Autowired
    EffectifJpaRepository effectifJpaRepository;

    @Autowired
    TypeStructureJpaRepository typeStructureJpaRepository;

    @Autowired
    StatutJuridiqueJpaRepository statutJuridiqueJpaRepository;

    @Autowired
    NafN5JpaRepository nafN5JpaRepository;

    @Autowired
    PaysJpaRepository paysJpaRepository;

    @Autowired
    AppConfigService appConfigService;

    @JsonView(Views.List.class)
    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC, AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Structure> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Structure> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(structureRepository.count(filters));
        paginatedResponse.setData(structureRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping(value = "/export/excel", produces = "application/vnd.ms-excel")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> exportExcel(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        byte[] bytes = structureRepository.exportExcel(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(bytes);
    }

    @GetMapping(value = "/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<String> exportCsv(@RequestParam(name = "headers", defaultValue = "{}") String headers, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        StringBuilder csv = structureRepository.exportCsv(headers, predicate, sortOrder, filters);
        return ResponseEntity.ok().body(csv.toString());
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC, AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public Structure getById(@PathVariable("id") int id) {
        Structure structure = structureJpaRepository.findById(id);
        if (structure == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Structure non trouvée");
        }
        return structure;
    }

    @PostMapping
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC, AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.CREATION})
    public Structure create(@Valid @RequestBody StructureFormDto structureFormDto) {
        Structure structure = new Structure();
        setStructureData(structure, structureFormDto);
        ContextDto contextDto = ServiceContext.getServiceContext();
        Utilisateur utilisateur = contextDto.getUtilisateur();
        if (!UtilisateurHelper.isRole(utilisateur, Role.ETU) || appConfigService.getConfigGenerale().isAutoriserValidationAutoOrgaAccCreaEtu()) {
            structure.setLoginValidation(utilisateur.getLogin());
            structure.setEstValidee(true);
            structure.setDateValidation(new Date());
        }
        return structureJpaRepository.saveAndFlush(structure);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC, AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION})
    public Structure update(@PathVariable("id") int id, @Valid @RequestBody StructureFormDto structureFormDto) {
        Structure structure = structureJpaRepository.findById(id);
        setStructureData(structure, structureFormDto);
        structure = structureJpaRepository.saveAndFlush(structure);
        return structure;
    }

    private void check(StructureFormDto structureFormDto) {
        if (structureFormDto.getCodeNafN5() == null && structureFormDto.getActivitePrincipale() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "L'un des 2 champs \"code APE\" ou \"activité principale\" doit être renseigné");
        }
    }

    private void setStructureData(Structure structure, StructureFormDto structureFormDto) {
        if (structure == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Structure non trouvée");
        }
        Effectif effectif = effectifJpaRepository.findById(structureFormDto.getIdEffectif());
        if (effectif == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Effectif non trouvé");
        }
        TypeStructure typeStructure = typeStructureJpaRepository.findById(structureFormDto.getIdTypeStructure());
        if (typeStructure == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Type de structure non trouvé");
        }
        StatutJuridique statutJuridique = statutJuridiqueJpaRepository.findById(structureFormDto.getIdStatutJuridique());
        if (statutJuridique == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Statut juridique non trouvé");
        }
        NafN5 nafN5 = null;
        if (structureFormDto.getCodeNafN5() != null && !structureFormDto.getCodeNafN5().isEmpty()) {
            nafN5 = nafN5JpaRepository.findByCode(structureFormDto.getCodeNafN5());
            if (nafN5 == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Code non trouvé");
            }
        }
        Pays pays = paysJpaRepository.findById(structureFormDto.getIdPays());
        if (pays == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Pays non trouvé");
        }

        check(structureFormDto);
        structure.setRaisonSociale(structureFormDto.getRaisonSociale());
        structure.setNumeroSiret(structureFormDto.getNumeroSiret());
        structure.setEffectif(effectif);
        structure.setTypeStructure(typeStructure);
        structure.setStatutJuridique(statutJuridique);
        structure.setNafN5(nafN5);
        structure.setActivitePrincipale(structureFormDto.getActivitePrincipale());
        structure.setVoie(structureFormDto.getVoie());
        structure.setCodePostal(structureFormDto.getCodePostal());
        structure.setBatimentResidence(structureFormDto.getBatimentResidence());
        structure.setCommune(structureFormDto.getCommune());
        structure.setLibCedex(structureFormDto.getLibCedex());
        structure.setPays(pays);
        structure.setMail(structureFormDto.getMail());
        structure.setTelephone(structureFormDto.getTelephone());
        structure.setSiteWeb(structureFormDto.getSiteWeb());
        structure.setFax(structureFormDto.getFax());
    }
}
