package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.config.properties.SireneProperties;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.SireneInfoDto;
import org.esup_portail.esup_stage.dto.StructureFormDto;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.dto.ImportReportDto;
import org.esup_portail.esup_stage.dto.LineErrorDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.security.permission.StructurePermissionEvaluator;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.Structure.StructureService;
import org.esup_portail.esup_stage.service.Structure.utils.CsvStructureImportUtils;
import org.esup_portail.esup_stage.service.sirene.SireneService;
import org.esup_portail.esup_stage.service.sirene.model.ListStructureSireneDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

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
    private SireneService sireneService;

    @Autowired
    private StructureService structureService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SireneProperties sireneProperties;

    @Autowired
    private CsvStructureImportUtils csvUtils;

    @JsonView(Views.List.class)
    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC, AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<Structure> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<Structure> paginatedResponse = new PaginatedResponse<>();
        List<Structure> structures = structureRepository.findPaginated(page, perPage, predicate, sortOrder, filters);
        paginatedResponse.setTotal(structureRepository.count(filters));
        Map filterMap;
        try {
            filterMap = objectMapper.readValue(filters, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        boolean estEtudiant = UtilisateurHelper.isRole(Objects.requireNonNull(ServiceContext.getUtilisateur()), Role.ETU);
        boolean creationEtudiantInterdite = !appConfigService.getConfigGenerale().isAutoriserEtudiantACreerEntrepriseFrance();
        boolean paysFranceIncluse = true;
        if (filterMap != null && filterMap.containsKey("pays.id")) {
            Object paysValue = ((Map<?,?>) filterMap.get("pays.id")).get("value");
            if (paysValue instanceof String) {
                // Cas d'un seul pays
                paysFranceIncluse = "82".equals(paysValue);
            } else if (paysValue instanceof List) {
                // Cas de plusieurs pays
                paysFranceIncluse = ((List<?>) paysValue).contains("82")
                        || ((List<?>) paysValue).stream().anyMatch(p -> "82".equals(String.valueOf(p)));
            } else {
                // Autre format, on convertit en String
                paysFranceIncluse = "82".equals(String.valueOf(paysValue));
            }
        }

        if (sireneProperties.isApiSireneActive()
                && structures.size() < sireneProperties.getNombreMinimumResultats()
                && (creationEtudiantInterdite || !estEtudiant)
                && paysFranceIncluse
                && (
                filterMap.size() >= 2
                        || (filterMap.size() == 1 && (
                        filterMap.containsKey("numeroSiret")
                                || filterMap.containsKey("pays.id")
                ))
        )
        ) {
            List<String> existingSirets = new ArrayList<>();
            structures.forEach(s -> existingSirets.add(s.getNumeroSiret()));
            if (page == 1 && structures.size() < sireneProperties.getNombreMinimumResultats()) {
                int manque = perPage - structures.size();
                ListStructureSireneDTO result = sireneService.getEtablissementFiltered(1, manque, filters);
                if (manque > 0) {
                    List<Structure> additional = result
                            .getStructures()
                            .stream()
                            .filter(s -> s.getNumeroSiret() == null
                                    || !existingSirets.contains(s.getNumeroSiret()))
                            .toList();
                    structures.addAll(additional);
                    paginatedResponse.setTotal(paginatedResponse.getTotal()+result.getTotal());
                }
            }
            else if (page > 1) {
                ListStructureSireneDTO result = sireneService.getEtablissementFiltered(page, perPage, filters);
                List<Structure> apiPage = result
                        .getStructures()
                        .stream()
                        .filter(s -> s.getNumeroSiret() == null
                                || !(existingSirets.contains(s.getNumeroSiret())))
                        .toList();
                structures.clear();
                structures.addAll(apiPage);
                paginatedResponse.setTotal(paginatedResponse.getTotal()+result.getTotal());
            }
        }
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
    public ResponseEntity<?> importStructures(InputStream inputStream) {
        logger.info("import start");

        final String separator = ";";
        ImportReportDto report = new ImportReportDto();
        int lineNumber = 0;
        boolean isHeader = true;

        try (BufferedReader br = csvUtils.openReader(inputStream)) { // <<— ici

            String line;
            CsvStructureImportUtils.Indices I = null;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                // Entête -> mapping indices
                if (isHeader) {
                    isHeader = false;
                    try {
                        I = csvUtils.mapHeaderIndices(line, separator);
                    } catch (IllegalArgumentException iae) {
                        report.setFatalError(iae.getMessage());
                        break;
                    }
                    continue;
                }

                report.setTotalLines(report.getTotalLines() + 1);

                if (line.isEmpty()) {
                    report.getErrors().add(new LineErrorDto(lineNumber, "Ligne", "Ligne vide", null));
                    continue;
                }

                String[] columns = line.split(separator, -1);
                var col = csvUtils.colAccessor(columns);

                var lineErrors = csvUtils.validateRow(lineNumber, col, I);

                csvUtils.duplicateError(lineNumber, col, I, structureJpaRepository)
                        .ifPresent(lineErrors::add);

                if (!lineErrors.isEmpty()) {
                    report.getErrors().addAll(lineErrors);
                    continue;
                }

                Structure structure = csvUtils.buildStructure(col, I);
                structureService.save(null, structure);
                report.setImported(report.getImported() + 1);
            }

        } catch (Exception e) {
            logger.error("Erreur fatale import CSV", e);
            report.setFatalError("Erreur import : " + e.getMessage());
        } finally {
            structureJpaRepository.flush();
            logger.info("import end");
        }

        if (report.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(report);
        }
        return ResponseEntity.ok().build();
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
        if (structureFormDto.getNumeroSiret() != null && !structureFormDto.getNumeroSiret().isEmpty()) {
            Structure existingStructure = structureJpaRepository.findBySiret(structureFormDto.getNumeroSiret());
            if (existingStructure != null && existingStructure.getTemEnServStructure()) {
                throw new AppException(HttpStatus.CONFLICT, "Le numéro de SIRET est déjà utilisé par une structure active : " + existingStructure.getRaisonSociale());
            }
        }
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
        structure.setTemEnServStructure(true);
        structure.setTemSiren(false);
        return structureService.save(null,structure);
    }

    @PutMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC, AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.MODIFICATION},evaluator = StructurePermissionEvaluator.class)
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
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        Structure structure;
        if(structureBody.getId() != null) {
            structure = structureJpaRepository.findById(structureBody.getId()).orElse(null);
            if (structure != null && structure.getTemEnServStructure()) {
                if(structure.getNumeroSiret() != null && !structure.getNumeroSiret().isEmpty() && !structure.isVerrouillageSynchroStructureSirene()) {
                    String jsonStructure;
                    try{
                        jsonStructure = objectMapper.writeValueAsString(structure);
                    }catch(JsonProcessingException e){
                        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR,"Erreur lors de la mise à jour de la structure");
                    }
                    sireneService.update(jsonStructure,structure);
                }
                return structure;
            }
        }

        boolean hasSiret = structureBody.getNumeroSiret() != null && !structureBody.getNumeroSiret().isEmpty();

        if (hasSiret) {
            // Cas structure avec SIRET
            structure = structureJpaRepository.findBySiret(structureBody.getNumeroSiret());

            if (structure != null && structure.getTemEnServStructure()) {
                // SIRET déjà utilisé par une structure active
                throw new AppException(HttpStatus.CONFLICT, "Le numéro de SIRET est déjà utilisé par la structure " + structure.getRaisonSociale());
            }

            // Sinon : SIRET non trouvé ou structure désactivée → on autorise la création
        } else {
            // Cas structure sans SIRET (ONG, asso, international)
            structure = structureJpaRepository.findByRaisonSociale(structureBody.getRaisonSociale());
            if (structure != null) {
                // Si une structure sans SIRET et avec même nom existe, on la retourne
                return structure;
            }

            // Si pas de raison sociale fournie : erreur
            if (structureBody.getRaisonSociale() == null || structureBody.getRaisonSociale().isEmpty()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "La raison sociale est obligatoire si le SIRET est vide");
            }
        }

        // Cas création d'une nouvelle structure
        if (!UtilisateurHelper.isRole(utilisateur, Role.ETU) || appConfigService.getConfigGenerale().isAutoriserValidationAutoOrgaAccCreaEtu()) {
            Date now = new Date();
            structureBody.setLoginCreation(utilisateur.getLogin());
            structureBody.setLoginValidation(utilisateur.getLogin());
            structureBody.setDateValidation(now);
            structureBody.setEstValidee(true);
        }
        structureBody.setTemEnServStructure(true);
        structureBody.setTemSiren(true);
        return structureService.save(null, structureBody);
    }


    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC, AppFonctionEnum.NOMENCLATURE}, droits = {DroitEnum.SUPPRESSION})
    public void delete(@PathVariable("id") int id) {
        Structure structure = structureJpaRepository.findById(id);
        if (structure == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Structure non trouvée");
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
                throw new AppException(HttpStatus.NOT_FOUND, "Code APE non trouvé");
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
        structure.setVerrouillageSynchroStructureSirene(structureFormDto.getVerrouillageSynchroStructureSirene());
    }

    @GetMapping("/sirene")
    public SireneInfoDto getSireneInfo(){
        SireneInfoDto sireneInfoDto = new SireneInfoDto();
        sireneInfoDto.setIsApiSireneActive(sireneProperties.isApiSireneActive());
        sireneInfoDto.setNombreResultats(sireneProperties.getNombreMinimumResultats());
        return sireneInfoDto;
    }

    @PatchMapping("{id}/sirene")
    @Secure(fonctions = {AppFonctionEnum.ORGA_ACC}, droits = {DroitEnum.MODIFICATION})
    public Structure updateFromSirene(@PathVariable("id") int id) {
        Structure structure = structureJpaRepository.findById(id);
        if (structure == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Structure non trouvée");
        }
        if(structure.getNumeroSiret() == null || structure.getNumeroSiret().isEmpty()){
            throw new AppException(HttpStatus.BAD_REQUEST,"Le numéro de SIRET est vide");
        }
        String jsonStructure;
        try{
            jsonStructure = objectMapper.writeValueAsString(structure);
        }catch(JsonProcessingException e){
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR,"Erreur lors de la mise à jour de la structure");
        }
        sireneService.update(jsonStructure,structure);
        return structure;
    }

}
