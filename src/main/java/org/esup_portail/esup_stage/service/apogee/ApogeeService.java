package org.esup_portail.esup_stage.service.apogee;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.esup_portail.esup_stage.config.properties.ReferentielProperties;
import org.esup_portail.esup_stage.dto.ConventionFormationDto;
import org.esup_portail.esup_stage.dto.LdapSearchDto;
import org.esup_portail.esup_stage.dto.RegimeInscriptionDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.CritereGestionJpaRepository;
import org.esup_portail.esup_stage.repository.EtapeJpaRepository;
import org.esup_portail.esup_stage.repository.TypeConventionJpaRepository;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.apogee.model.*;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApogeeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApogeeService.class);
    private final WebClient webClient;
    @Autowired
    ReferentielProperties referentielProperties;
    @Autowired
    AppConfigService appConfigService;
    @Autowired
    TypeConventionJpaRepository typeConventionJpaRepository;
    @Autowired
    EtapeJpaRepository etapeJpaRepository;
    @Autowired
    CritereGestionJpaRepository critereGestionJpaRepository;
    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;
    @Autowired
    LdapService ldapService;

    public ApogeeService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    private String call(String api, Map<String, String> params) {
        try {
            LOGGER.info("Apogee " + api + " parametres: " + "{" + params.keySet().stream().map(key -> key + "=" + params.get(key)).collect(Collectors.joining(", ", "{", "}")) + "}");
            String urlWithQuery = referentielProperties.getApogeeUrl() + api;
            List<String> listParams = new ArrayList<>();
            params.forEach((key, value) -> {
                if (!Strings.isEmpty(value))
                    listParams.add(key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
            });
            if (listParams.isEmpty()) {
                urlWithQuery += "?" + String.join("&", listParams);
            }

            String response = webClient.get()
                    .uri(urlWithQuery)
                    .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((referentielProperties.getLogin() + ":" + referentielProperties.getPassword()).getBytes()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(WebClientResponseException.class, ex -> ex.getRawStatusCode() == HttpStatus.NOT_FOUND.value() ? Mono.just("") : Mono.error(ex))
                    .block();
            if (Strings.isEmpty(response)) {
                throw new AppException(HttpStatus.NOT_FOUND, "Aucune donnée trouvée");
            }
            return response;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'appel au ws Apogee " + api + ": " + e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }

    public EtudiantRef getInfoApogee(String numEtudiant, String annee) {
        Map<String, String> params = new HashMap<>();
        params.put("codEtud", numEtudiant);
        params.put("annee", annee);
        String response = call("/etudiantRef", params);
        try {
            ObjectMapper mapper = new ObjectMapper();
            EtudiantRef etudiantRef = mapper.readValue(response, EtudiantRef.class);
            if (etudiantRef == null) {
                LOGGER.info("Aucun étudiant trouvé avec les paramètres");
            } else {
                // Si mail étudiant vide, on le récupère depuis le LDAP
                if (Strings.isEmpty(etudiantRef.getMail())) {
                    LdapSearchDto ldapSearchDto = new LdapSearchDto();
                    ldapSearchDto.setCodEtu(numEtudiant);
                    List<LdapUser> ldapEtudiant = ldapService.search("/etudiant", ldapSearchDto);
                    if (ldapEtudiant != null && ldapEtudiant.isEmpty()) {
                        etudiantRef.setMail(ldapEtudiant.get(0).getMail());
                    }
                }
            }
            return etudiantRef;
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur lors de la lecture de la réponse sur l'api etudiantRef: " + e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }

    public List<Composante> getListComposante() {
        List<Composante> list = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        String response = call("/composantesPrincipalesRef", params);
        try {
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(response);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Composante composante = new Composante();
                composante.setCode(entry.getKey());
                composante.setLibelle(entry.getValue().toString());
                list.add(composante);
            }
            if (list.isEmpty()) {
                LOGGER.info("Aucune composante trouvée");
            }
            return list;
        } catch (JsonParseException e) {
            LOGGER.error("Erreur lors de la lecture de la réponse sur l'api composantesPrincipalesRef: " + e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }

    public List<EtapeApogee> getListEtape() {
        List<EtapeApogee> list = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        String response = call("/etapesReference", params);
        try {
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(response);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                // 0: code Etape, 1: code version Etape
                String[] codes = entry.getKey().split(";");
                EtapeApogee etapeApogee = new EtapeApogee();
                etapeApogee.setCode(codes[0]);
                etapeApogee.setCodeVrsEtp(codes[1]);
                etapeApogee.setLibelle(entry.getValue().toString());
                list.add(etapeApogee);
            }
            if (list.isEmpty()) {
                LOGGER.info("Aucune étape trouvée");
            }
            return list;
        } catch (JsonParseException e) {
            LOGGER.error("Erreur lors de la lecture de la réponse sur l'api etapesReference: " + e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }

    public DiplomeEtape[] getListDiplomeEtape(String codeComposante, String codeAnnee) {
        Map<String, String> params = new HashMap<>() {{
            put("codeComposante", codeComposante);
            put("codeAnnee", codeAnnee);
        }};
        String response = call("/diplomesReferenceParComposanteEtAnnee", params);
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response, DiplomeEtape[].class);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la lecture de la réponse sur l'api etapesReference: " + e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }

    public List<String> getAnneeInscriptions(String numEtudiant) {
        Map<String, String> params = new HashMap<>();
        params.put("codEtud", numEtudiant);
        String response = call("/anneesIa", params);
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> annees = Arrays.asList(mapper.readValue(response, String[].class));
            if (annees.isEmpty()) {
                LOGGER.info("Aucune année trouvée");
            }
            return annees;
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur lors de la lecture de la réponse sur l'api anneesIa: " + e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }

    public ApogeeMap getEtudiantEtapesInscription(String numEtudiant, String annee) {
        Map<String, String> params = new HashMap<>();
        params.put("codEtud", numEtudiant);
        params.put("annee", annee);
        String response = call("/etapesByEtudiantAndAnnee", params);
        try {
            ObjectMapper mapper = new ObjectMapper();
            ApogeeMap apogeeMap = mapper.readValue(response, ApogeeMap.class);
            if (apogeeMap == null) {
                LOGGER.info("Aucune donnée trouvée");
            }
            return apogeeMap;
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur lors de la lecture de la réponse sur l'api etapesByEtudiantAndAnnee: " + e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }

    public List<ConventionFormationDto> getInscriptions(Utilisateur utilisateur, String numEtudiant, String anneeConvention) {
        List<String> anneesFiltrees = getAnneesFiltrees(utilisateur, numEtudiant, anneeConvention);
        List<ConventionFormationDto> inscriptions = new ArrayList<>();

        for (String annee : anneesFiltrees) {
            ApogeeMap apogeeMap = getEtudiantEtapesInscription(numEtudiant, annee);
            RegimeInscription regimeInscription = findRegimeInscription(apogeeMap, annee);
            List<TypeConvention> typeConventionsCompatibles = resolveTypeConventionsCompatibles(regimeInscription);
            List<TypeConvention> typeConventionsAuto = resolveTypeConventionsAuto(regimeInscription);
            List<ConventionFormationDto> inscriptionsAnnee = buildInscriptionsForAnnee(apogeeMap, annee, typeConventionsCompatibles, typeConventionsAuto);
            enrichWithElementsPedagogiques(inscriptionsAnnee, apogeeMap.getListeELPs());
            inscriptions.addAll(inscriptionsAnnee);
        }

        return filterInscriptions(utilisateur, inscriptions);
    }

    private List<String> getAnneesFiltrees(Utilisateur utilisateur, String numEtudiant, String anneeConvention) {
        int anneeEnCoursInt = Integer.parseInt(appConfigService.getAnneeUniv());
        List<String> annees = getAnneeInscriptions(numEtudiant);

        annees = annees.stream()
                .filter(a -> isAnneeAutorisee(a, anneeEnCoursInt, UtilisateurHelper.isRole(utilisateur, Role.ETU)))
                .collect(Collectors.toList());

        if (anneeConvention != null && !anneeConvention.isEmpty() && !annees.contains(anneeConvention)) {
            annees.add(anneeConvention);
        }

        return annees;
    }

    private boolean isAnneeAutorisee(String annee, int anneeEnCoursInt, boolean isEtudiant) {
        int anneeInt = Integer.parseInt(annee);
        boolean isCourranteOuFuture = anneeInt >= anneeEnCoursInt;
        boolean isPrecedente = anneeInt == anneeEnCoursInt - 1;
        return isCourranteOuFuture || (!isEtudiant && isPrecedente);
    }

    private RegimeInscription findRegimeInscription(ApogeeMap apogeeMap, String annee) {
        return apogeeMap.getRegimeInscription().stream()
                .filter(r -> r.getAnnee().equals(annee))
                .findAny()
                .orElse(null);
    }

    private List<TypeConvention> resolveTypeConventionsCompatibles(RegimeInscription regimeInscription) {
        if (regimeInscription == null || regimeInscription.getCodRegIns() == null || regimeInscription.getCodRegIns().isEmpty()) {
            return List.of();
        }

        return typeConventionJpaRepository.findAllActiveCompatibleByCodeRegimeInscription(regimeInscription.getCodRegIns());
    }

    private List<TypeConvention> resolveTypeConventionsAuto(RegimeInscription regimeInscription) {
        if (regimeInscription == null || regimeInscription.getCodRegIns() == null || regimeInscription.getCodRegIns().isEmpty()) {
            return List.of();
        }

        return typeConventionJpaRepository.findAllActiveByCodeRegimeInscription(regimeInscription.getCodRegIns());
    }

    private List<ConventionFormationDto> buildInscriptionsForAnnee(ApogeeMap apogeeMap, String annee, List<TypeConvention> typeConventionsCompatibles, List<TypeConvention> typeConventionsAuto) {
        List<ConventionFormationDto> inscriptions = new ArrayList<>();

        for (EtapeInscription etapeInscription : apogeeMap.getListeEtapeInscriptions()) {
            syncEtapeIfAbsent(etapeInscription);

            CentreGestion centreGestion = resolveCentreGestion(etapeInscription);
            if (centreGestion == null) continue;

            List<TypeConvention> typeConventionsDisponibles = resolveTypeConventionsDisponibles(etapeInscription, typeConventionsCompatibles);
            TypeConvention typeEffectif = resolveTypeConventionForInscription(etapeInscription, centreGestion, typeConventionsAuto);

            ConventionFormationDto dto = new ConventionFormationDto();
            dto.setEtapeInscription(etapeInscription);
            dto.setAnnee(annee);
            dto.setTypeConvention(typeEffectif);
            dto.setTypeConventionsDisponibles(typeConventionsDisponibles);
            dto.setCentreGestion(centreGestion);
            inscriptions.add(dto);
        }

        return inscriptions;
    }

    private List<TypeConvention> resolveTypeConventionsDisponibles(EtapeInscription etapeInscription, List<TypeConvention> typeConventionsCompatibles) {
        TypeConvention typeConventionCesure = changeTypeConventionByCodeCursus(etapeInscription.getCodeCursusAmenage());
        if (typeConventionCesure != null) {
            return List.of(typeConventionCesure);
        }

        if (!typeConventionsCompatibles.isEmpty()) {
            return typeConventionsCompatibles;
        }

        return typeConventionJpaRepository.findAllActiveWithTemplate();
    }

    private TypeConvention resolveTypeConventionForInscription(EtapeInscription etapeInscription, CentreGestion centreGestion, List<TypeConvention> typeConventions) {
        if (centreGestion.isDesactiverSelectionAutomatiqueTypeConvention()) {
            return null;
        }

        TypeConvention typeConventionCesure = changeTypeConventionByCodeCursus(etapeInscription.getCodeCursusAmenage());
        if (typeConventionCesure != null) {
            return typeConventionCesure;
        }

        if (typeConventions.size() == 1) {
            return typeConventions.get(0);
        }

        return null;
    }

    public TypeConvention resolveTypeConvention(RegimeInscription regimeInscription, EtapeInscription etapeInscription, CentreGestion centreGestion) {
        List<TypeConvention> typeConventions = resolveTypeConventionsAuto(regimeInscription);
        return resolveTypeConventionForInscription(etapeInscription, centreGestion, typeConventions);
    }

    private void syncEtapeIfAbsent(EtapeInscription etapeInscription) {
        String codeUniversite = appConfigService.getConfigGenerale().getCodeUniversite();
        Etape etape = etapeJpaRepository.findById(etapeInscription.getCodeEtp(), etapeInscription.getCodVrsVet(), codeUniversite);
        if (etape != null) return;

        EtapeId etapeId = new EtapeId();
        etapeId.setCode(etapeInscription.getCodeEtp());
        etapeId.setCodeVersionEtape(etapeInscription.getCodVrsVet());
        etapeId.setCodeUniversite(codeUniversite);

        etape = new Etape();
        etape.setId(etapeId);
        etape.setLibelle(etapeInscription.getLibWebVet());
        etapeJpaRepository.saveAndFlush(etape);
    }

    private CentreGestion resolveCentreGestion(EtapeInscription etapeInscription) {
        CritereGestion critere = critereGestionJpaRepository.findEtapeById(etapeInscription.getCodeEtp(), etapeInscription.getCodVrsVet());

        if (critere == null) {
            critere = critereGestionJpaRepository.findEtapeById(etapeInscription.getCodeComposante(), "");
        }

        if (critere != null) {
            return critere.getCentreGestion();
        }

        if (appConfigService.getConfigGenerale().isAutoriserConventionsOrphelines()) {
            return centreGestionJpaRepository.getCentreEtablissement();
        }

        return null;
    }

    private void enrichWithElementsPedagogiques(List<ConventionFormationDto> inscriptions, List<ElementPedagogique> elements) {
        for (ElementPedagogique elp : elements) {
            inscriptions.stream()
                    .filter(i -> i.getEtapeInscription().getCodeEtp().equals(elp.getCodEtp())
                            && i.getEtapeInscription().getCodVrsVet().equals(elp.getCodVrsVet()))
                    .findAny()
                    .ifPresent(dto -> dto.getElementPedagogiques().add(elp));
        }
    }

    private List<ConventionFormationDto> filterInscriptions(Utilisateur utilisateur, List<ConventionFormationDto> inscriptions) {
        if (UtilisateurHelper.isRole(utilisateur, Role.ADM)) {
            return inscriptions;
        }

        int anneeEnCoursInt = Integer.parseInt(appConfigService.getAnneeUniv());
        boolean isEtudiant = UtilisateurHelper.isRole(utilisateur, Role.ETU);

        return inscriptions.stream()
                .filter(inscription -> isAutorisationEtudiantRespectee(inscription, isEtudiant))
                .filter(inscription -> isAnneeCentreGestionAutorisee(inscription, anneeEnCoursInt, isEtudiant))
                .toList();
    }

    private boolean isAutorisationEtudiantRespectee(ConventionFormationDto inscription, boolean isEtudiant) {
        return !isEtudiant || inscription.getCentreGestion().isAutorisationEtudiantCreationConvention();
    }

    private boolean isAnneeCentreGestionAutorisee(ConventionFormationDto inscription, int anneeEnCoursInt, boolean isEtudiant) {
        int anneeInscriptionInt = Integer.parseInt(inscription.getAnnee());
        if (anneeInscriptionInt >= anneeEnCoursInt) {
            return true;
        }

        if (!Boolean.TRUE.equals(inscription.getCentreGestion().getRecupInscriptionAnterieure())) {
            return false;
        }

        return !isEtudiant && anneeInscriptionInt == anneeEnCoursInt - 1;
    }

    public TypeConvention changeTypeConventionByCodeCursus(String codeCursusAmenage) {
        if (codeCursusAmenage != null && !codeCursusAmenage.isEmpty() && appConfigService.getConfigGenerale().getCodeCesure() != null && !appConfigService.getConfigGenerale().getCodeCesure().isEmpty()) {
            List<String> codeCesureList = List.of(appConfigService.getConfigGenerale().getCodeCesure().split(";"));
            if (codeCesureList.contains(codeCursusAmenage)) {
                return typeConventionJpaRepository.findByCodeCtrl("CESURE");
            }
        }
        return null;
    }

    public EtudiantDiplomeEtapeResponse[] getEtudiantsParDiplomeEtape(EtudiantDiplomeEtapeSearch search) {
        Map<String, String> params = new HashMap<>();
        params.put("annee", search.getAnnee());
        params.put("codeEtape", search.getCodeEtape());
        params.put("versionEtape", search.getVersionEtape());
        params.put("codeDiplome", search.getCodeDiplome());
        params.put("versionDiplome", search.getVersionDiplome());
        params.put("codEtu", search.getCodEtu());
        params.put("nom", search.getNom());
        params.put("prenom", search.getPrenom());
        String response = call("/listEtuParEtapeEtDiplome", params);
        try {
            ObjectMapper mapper = new ObjectMapper();
            EtudiantDiplomeEtapeResponse[] apogeeMap = mapper.readValue(response, EtudiantDiplomeEtapeResponse[].class);
            if (apogeeMap == null) {
                LOGGER.info("Aucune donnée trouvée");
            }
            return apogeeMap;
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur lors de la lecture de la réponse sur l'api etapesByEtudiantAndAnnee: " + e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }

    public InfosAdmEtu getInfosAdmEtudiant(String numEtud) {
        Map<String, String> params = new HashMap<>();
        params.put("numEtud", numEtud);
        String response = call("/infosAdmEtu", params);
        try {
            ObjectMapper mapper = new ObjectMapper();
            InfosAdmEtu infoAdmEtudiant = mapper.readValue(response, InfosAdmEtu.class);
            if (infoAdmEtudiant == null) {
                LOGGER.info("Aucune donnée trouvée");
            }
            return infoAdmEtudiant;
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur lors de la lecture de la réponse sur l'api infoAdmEtudiant: " + e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }

    public List<RegimeInscriptionDto> getRegimesInscriptions() {
        String response = call("/regimesInscriptions", new HashMap<>());

        try {
            ObjectMapper mapper = new ObjectMapper();

            Map<String, String> rawMap = mapper.readValue(
                    response,
                    mapper.getTypeFactory().constructMapType(Map.class, String.class, String.class)
            );

            List<RegimeInscriptionDto> result = rawMap.entrySet().stream()
                    .map(entry -> new RegimeInscriptionDto(entry.getKey(), entry.getValue()))
                    .toList();

            if (result.isEmpty()) {
                LOGGER.info("Aucune donnée trouvée");
            }

            return result;
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur lors de la lecture de la réponse sur l'api regInsList: " + e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }
}
