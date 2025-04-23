package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.esup_portail.esup_stage.service.Structure.StructureService;
import org.esup_portail.esup_stage.service.siren.SirenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiController
@RequestMapping("/structures")
public class StructureController {

    private static final Logger logger = LogManager.getLogger(ConsigneController.class);

    @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private StructureJpaRepository structureJpaRepository;

    @Autowired
    private EffectifJpaRepository effectifJpaRepository;

    @Autowired
    private TypeStructureJpaRepository typeStructureJpaRepository;

    @Autowired
    private StatutJuridiqueJpaRepository statutJuridiqueJpaRepository;

    @Autowired
    private NafN5JpaRepository nafN5JpaRepository;

    @Autowired
    private PaysJpaRepository paysJpaRepository;

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private SirenService sirenService;

    @Autowired
    private StructureService structureService;

    @Autowired
    private ConventionJpaRepository conventionJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @JsonView(Views.List.class)
    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC, AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Structure> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Structure> paginatedResponse = new PaginatedResponse<>();
        List<Structure> structures = structureRepository.findPaginated(page, perPage, predicate, sortOrder, filters);

        if(structures.size() < 1 && !appConfigService.getConfigGenerale().isAutoriserEtudiantACreerEntreprise()){
            List<String> existingSirets = new ArrayList<String>();
            structures.forEach(s -> existingSirets.add(s.getNumeroSiret()));
            List<Structure> additionalStructures = sirenService.getEtablissementFiltered(filters);
            additionalStructures = additionalStructures.stream()
                    .filter(s -> s.getNumeroSiret() == null || !existingSirets.contains(s.getNumeroSiret()))
                    .toList();
            structures.addAll(additionalStructures);
        }

        paginatedResponse.setTotal((long) structures.size());
        paginatedResponse.setData(structures);
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

    @PostMapping(value = "/import", consumes = "text/csv")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public void importStructures(InputStream inputStream) {

        logger.info("import start");

        int indexNumeroRNE = 0;
        int indexRaisonSociale = 1;
        int indexNumeroSiret = 39;
        int indexActivitePrincipale = 62;
        int indexVoie = 4;
        int indexCodePostal = 7;
        int indexCommune = 10;
        int indexTelephone = 19;
        int indexFax = 20;
        int indexSiteWeb = 21;
        int indexMail = 22;

        Effectif effectif = effectifJpaRepository.findByLibelle("Inconnu");
        if (effectif == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Effectif non trouvé");
        }
        TypeStructure typeStructure = typeStructureJpaRepository.findByLibelle("Etablissement d'enseignement");
        if (typeStructure == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Type de structure non trouvé");
        }
        StatutJuridique statutJuridique = statutJuridiqueJpaRepository.findByLibelle("public");
        if (statutJuridique == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Statut juridique non trouvé");
        }
        NafN5 nafN5 = nafN5JpaRepository.findByCode("85.59B");
        if (nafN5 == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Code non trouvé");
        }

        Pays pays = paysJpaRepository.findByIso2("FR");
        if (pays == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Pays non trouvé");
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = "";
            String separator = ";";
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                } else {
                    Structure structure = new Structure();

                    structure.setNafN5(nafN5);
                    structure.setEffectif(effectif);
                    structure.setStatutJuridique(statutJuridique);
                    structure.setTypeStructure(typeStructure);
                    structure.setPays(pays);

                    String[] columns = line.split(separator, -1);
                    for (int i = 0; i < columns.length; i++) {

                        if (i == indexNumeroRNE) {
                            structure.setNumeroRNE(columns[indexNumeroRNE]);
                        }
                        if (i == indexRaisonSociale) {
                            structure.setRaisonSociale(columns[indexRaisonSociale]);
                        }
                        if (i == indexNumeroSiret) {
                            structure.setNumeroSiret(columns[indexNumeroSiret]);
                        }
                        if (i == indexActivitePrincipale) {
                            structure.setActivitePrincipale(columns[indexActivitePrincipale]);
                        }
                        if (i == indexVoie) {
                            structure.setVoie(columns[indexVoie]);
                        }
                        if (i == indexCodePostal) {
                            structure.setCodePostal(columns[indexCodePostal]);
                        }
                        if (i == indexCommune) {
                            structure.setCommune(columns[indexCommune]);
                        }
                        if (i == indexMail) {
                            structure.setMail(columns[indexMail]);
                        }
                        if (i == indexTelephone) {
                            structure.setTelephone(columns[indexTelephone]);
                        }
                        if (i == indexSiteWeb) {
                            structure.setSiteWeb(columns[indexSiteWeb]);
                        }
                        if (i == indexFax) {
                            structure.setFax(columns[indexFax]);
                        }
                    }

                    Structure existant = structureJpaRepository.findByRNE(structure.getNumeroRNE());

                    if (existant == null) {
                        structureService.save(null,structure);
                    } else {
                        logger.info("skipped existing structure with RNE : " + existant.getNumeroRNE());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        structureJpaRepository.flush();
        logger.info("import end");
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
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (!UtilisateurHelper.isRole(utilisateur, Role.ETU) || appConfigService.getConfigGenerale().isAutoriserValidationAutoOrgaAccCreaEtu()) {
            structure.setLoginCreation(utilisateur.getLogin());
            structure.setDateValidation(new Date());
            structure.setLoginValidation(utilisateur.getLogin());
            structure.setEstValidee(true);
            structure.setDateValidation(new Date());
        }
        return structureService.save(null,structure);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC, AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION})
    public Structure update(@PathVariable("id") int id, @Valid @RequestBody StructureFormDto structureFormDto) {
        Structure structure = structureJpaRepository.findById(id);
        if (structure == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Structure non trouvée");
        }
        String oldStructure;
        try {
            oldStructure = objectMapper.writeValueAsString(structure);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur lors de la sérialisation de la structure d'origine", e);
        }
        setStructureData(structure, structureFormDto);
        structure = structureService.save(oldStructure,structure);
        return structure;
    }

    @PostMapping("/getOrCreate")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC, AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public Structure getOrCreate(@Valid @RequestBody Structure structureBody) {
        Structure structure;
        // Contrôle pour les établissements sans SIRET (ONG)
        if (structureBody.getNumeroSiret().isEmpty()) {
            structure = structureJpaRepository.findByRaisonSociale(structureBody.getRaisonSociale());
            if (structure != null) {
                return structure;
            }
            throw new AppException(HttpStatus.BAD_REQUEST,"Le numero de Siret est manquant");
        }
        // Gestion du cas où le SIRET est déjà utilisé
        structure = structureJpaRepository.findBySiret(structureBody.getNumeroSiret());
        if (structure != null) {
            // Si la structure existe mais est désactivée, on la réactive
            if (!structure.getTemEnServStructure()) {
                structure.setTemEnServStructure(true);
                return structureService.save(null,structure);
            }

        }
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (!UtilisateurHelper.isRole(utilisateur, Role.ETU) || appConfigService.getConfigGenerale().isAutoriserValidationAutoOrgaAccCreaEtu()) {
            structureBody.setLoginCreation(utilisateur.getLogin());
            structureBody.setDateValidation(new Date());
            structureBody.setLoginValidation(utilisateur.getLogin());
            structureBody.setEstValidee(true);
            structureBody.setDateValidation(new Date());
        }
        return structureService.save(null,structureBody);
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC, AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Structure structure = structureJpaRepository.findById(id);
        if (structure == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Structure non trouvée");
        }
        if(!conventionJpaRepository.findByStructureId(structure.getId()).isEmpty()) {
            throw new AppException(HttpStatus.FORBIDDEN, "La structure est relier à une convention");
        }
        structureService.delete(structure);
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
        // Contrôle SIRET non déjà existant
//        if (structureFormDto.getNumeroSiret() != null && structureRepository.existsSiret(structure, structureFormDto.getNumeroSiret())) {
//            throw new AppException(HttpStatus.BAD_REQUEST, "Le numéro SIRET existe déjà");
//        }
        // Contrôle de la validité du SIRET
        // - cas de La Poste (SIREN commençant par 356000000) : la somme des 14 chiffres du SIRET doit être un multiple de 5
        // - cas classique : algorithme de Luhn
        if (structureFormDto.getNumeroSiret() != null) {
            if (structureFormDto.getNumeroSiret().startsWith("356000000")) {
                int total = 0;
                for (int i = 0; i < structureFormDto.getNumeroSiret().length(); ++i) {
                    try {
                        total += Integer.parseInt(String.valueOf(structureFormDto.getNumeroSiret().charAt(i)));
                    } catch (NumberFormatException e) {
                        throw new AppException(HttpStatus.BAD_REQUEST, "Le numéro SIRET est invalide");
                    }
                }
                if (total % 5 != 0) {
                    throw new AppException(HttpStatus.BAD_REQUEST, "Le numéro SIRET est invalide");
                }
            } else {
                if (!LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(structureFormDto.getNumeroSiret())) {
                    throw new AppException(HttpStatus.BAD_REQUEST, "Le numéro SIRET est invalide");
                }
            }
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
        structure.setNumeroRNE(structureFormDto.getNumeroRNE());
    }

}
