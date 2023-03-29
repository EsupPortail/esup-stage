package org.esup_portail.esup_stage.service.apogee;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.dto.ConventionFormationDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.CritereGestionJpaRepository;
import org.esup_portail.esup_stage.repository.EtapeJpaRepository;
import org.esup_portail.esup_stage.repository.TypeConventionJpaRepository;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.apogee.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApogeeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApogeeService.class);

    @Autowired
    ApplicationBootstrap applicationBootstrap;

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

    private String call(String api, Map<String, String> params) {
        HttpURLConnection con;

        try {
            LOGGER.info("Apogee " + api + " parametres: " + "{" + params.keySet().stream().map(key -> key + "=" + params.get(key)).collect(Collectors.joining(", ", "{", "}")) + "}");
            String urlWithQuery = applicationBootstrap.getAppConfig().getReferentielWsApogeeUrl() + api;
            List<String> listParams = new ArrayList<>();
            params.forEach((key, value) -> listParams.add(key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8)));
            if (listParams.size() > 0) {
                urlWithQuery += "?" + String.join("&", listParams);
            }
            URL url = new URL(urlWithQuery);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((applicationBootstrap.getAppConfig().getReferentielWsLogin() + ":" + applicationBootstrap.getAppConfig().getReferentielWsPassword()).getBytes()));

            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur code " + con.getResponseCode() + " " + urlWithQuery);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                return response.toString();
            }

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
            if (list.size() == 0) {
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
            if (list.size() == 0) {
                LOGGER.info("Aucune étape trouvée");
            }
            return list;
        } catch (JsonParseException e) {
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
            if (annees.size() == 0) {
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
        String anneeEnCours = appConfigService.getAnneeUniv();
        List<ConventionFormationDto> inscriptions = new ArrayList<>();
        List<String> anneeInscriptions = getAnneeInscriptions(numEtudiant);

        // Filtre la liste des années universitaire pour lesquels on doit rechercher les inscriptions
        // Pour les étudiants, pas d'autorisation sur l'année précédente
        // Pour les gestionnaire, au plus autorisation sur l'année précédente
        int anneeEnCoursInt = Integer.parseInt(anneeEnCours);
        if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            anneeInscriptions = anneeInscriptions.stream().filter(a -> a.equals(anneeEnCours) || Integer.parseInt(a) > anneeEnCoursInt).collect(Collectors.toList());
        } else {
            anneeInscriptions = anneeInscriptions.stream().filter(a -> a.equals(anneeEnCours) || Integer.parseInt(a) > anneeEnCoursInt || anneeEnCoursInt - 1 == Integer.parseInt(a)).collect(Collectors.toList());
        }
        if (anneeConvention != null && !anneeConvention.isEmpty() && !anneeInscriptions.contains(anneeConvention)) {
            anneeInscriptions.add(anneeConvention);
        }

        for (String annee : anneeInscriptions) {
            ApogeeMap apogeeMap = getEtudiantEtapesInscription(numEtudiant, annee);
            RegimeInscription regIns = apogeeMap.getRegimeInscription().stream().filter(r -> r.getAnnee().equals(annee)).findAny().orElse(null);
            TypeConvention typeConvention = null;
            if (regIns != null) {
                typeConvention = typeConventionJpaRepository.findByCodeCtrl(regIns.getLicRegIns());
            }
            for (EtapeInscription etapeInscription : apogeeMap.getListeEtapeInscriptions()) {
                Etape etape = etapeJpaRepository.findById(etapeInscription.getCodeEtp(), etapeInscription.getCodVrsVet(), appConfigService.getConfigGenerale().getCodeUniversite());

                // alimentation de la table Etape avec celles remontées depuis Apogée
                if (etape == null) {
                    EtapeId etapeId = new EtapeId();
                    etapeId.setCode(etapeInscription.getCodeEtp());
                    etapeId.setCodeVersionEtape(etapeInscription.getCodVrsVet());
                    etapeId.setCodeUniversite(appConfigService.getConfigGenerale().getCodeUniversite());

                    etape = new Etape();
                    etape.setId(etapeId);
                    etape.setLibelle(etapeInscription.getLibWebVet());
                    etapeJpaRepository.saveAndFlush(etape);
                }
                ConventionFormationDto conventionFormationDto = new ConventionFormationDto();
                conventionFormationDto.setEtapeInscription(etapeInscription);
                conventionFormationDto.setAnnee(annee);
                String codeCursusAmenage = etapeInscription.getCodeCursusAmenage();
                if(codeCursusAmenage != null && !codeCursusAmenage.isEmpty()){
                    List<String> codeCesureList = List.of(appConfigService.getConfigGenerale().getCodeCesure().split(";"));
                    if (codeCesureList.contains(codeCursusAmenage))
                        if (typeConventionJpaRepository.findByCodeCtrl(codeCursusAmenage) != null )
                            typeConvention = typeConventionJpaRepository.findByCodeCtrl(codeCursusAmenage);
                }
                conventionFormationDto.setTypeConvention(typeConvention);
                CentreGestion centreGestion = null;
                // Recherche du centre de gestion par codeEtape/versionEtape
                CritereGestion critereGestion = critereGestionJpaRepository.findEtapeById(etapeInscription.getCodeEtp(), etapeInscription.getCodVrsVet());
                // Si non trouvé, recherche par code composante et version = ""
                if (critereGestion == null) {
                    critereGestion = critereGestionJpaRepository.findEtapeById(etapeInscription.getCodeComposante(), "");
                }
                // Si non trouvé on vérifie l'autorisation de création de convention non liée à un centre
                if (critereGestion == null) {
                    // récupération du centre de gestion établissement si autorisation de création d'une convention non rattachée à un centre
                    if (appConfigService.getConfigGenerale().isAutoriserConventionsOrphelines()) {
                        centreGestion = centreGestionJpaRepository.getCentreEtablissement();
                    }
                } else {
                    centreGestion = critereGestion.getCentreGestion();
                }
                if (centreGestion != null) {
                    conventionFormationDto.setCentreGestion(centreGestion);
                    inscriptions.add(conventionFormationDto);
                }
            }
            for (ElementPedagogique elementPedagogique : apogeeMap.getListeELPs()) {
                ConventionFormationDto conventionFormationDto = inscriptions.stream().filter(i -> i.getEtapeInscription().getCodeEtp().equals(elementPedagogique.getCodEtp()) && i.getEtapeInscription().getCodVrsVet().equals(elementPedagogique.getCodVrsVet())).findAny().orElse(null);
                if (conventionFormationDto != null) {
                    conventionFormationDto.getElementPedagogiques().add(elementPedagogique);
                }
            }
        }
        // On supprime les formations sans ELP si la config n'autorise pas la création de convention sans ELP
        if (!appConfigService.getConfigGenerale().isAutoriserElementPedagogiqueFacultatif()) {
            inscriptions = inscriptions.stream().filter(i -> i.getElementPedagogiques().size() > 0).collect(Collectors.toList());
        }
        if (!UtilisateurHelper.isRole(utilisateur, Role.ADM)) {
            if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
                // On garde les formations dont le centre de gestion autorise la création d'une convention
                inscriptions = inscriptions.stream().filter(i -> i.getCentreGestion().isAutorisationEtudiantCreationConvention()).collect(Collectors.toList());
            }
            // Filtre les inscriptions on fonction du paramétrage côté centre de gestion
            inscriptions = inscriptions.stream().filter(i -> {
                CentreGestion centreGestion = i.getCentreGestion();
                Boolean autorisationAnneePrecedente = centreGestion.getRecupInscriptionAnterieure();
                // On autorise la création de convention sur l'année en cours et les années suivantes
                int anneeInt = Integer.parseInt(i.getAnnee());
                if (i.getAnnee().equals(anneeEnCours) || anneeInt > anneeEnCoursInt) {
                    return true;
                }
                if (!autorisationAnneePrecedente) {
                    return false;
                } else {
                    // On autorise uniquement les gestionnaires pour l'année précédentes (et pas toutes les années précédentes)
                    if (UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
                        return false;
                    } else {
                        return (anneeEnCoursInt - 1) == anneeInt;
                    }
                }
            }).collect(Collectors.toList());
        }

        return inscriptions;
    }
}
