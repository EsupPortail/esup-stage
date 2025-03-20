package org.esup_portail.esup_stage.service.siren;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.esup_portail.esup_stage.config.properties.SirenProperties;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.service.siren.model.SirenResponse;
import org.esup_portail.esup_stage.service.siren.utils.SirenMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class SirenService {

    private static final Logger logger = LoggerFactory.getLogger(SirenService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SirenMapper sirenMapper;

    @Autowired
    private SirenProperties sirenProperties;

    public SirenService() {
        this.restTemplate = new RestTemplate();
    }

    public Structure getEtablissement(String siret) {
        String url = sirenProperties.getUrl() + "/siret/" + siret;
        logger.info("Requête vers {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-INSEE-Api-Key-Integration", sirenProperties.getToken());
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<SirenResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, SirenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return sirenMapper.toStructureList(response.getBody()).getFirst();
            } else {
                logger.warn("Réponse non valide : {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'établissement avec SIRET {}", siret, e);
            return null;
        }
    }

    public List<Structure> getEtablissementFiltered(String filters) {
        String baseUrl = sirenProperties.getUrl();
        String query = buildQueryFromFilters(filters);
        String url = baseUrl + "?q=" + query;
        logger.info("Requête filtrée vers {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-INSEE-Api-Key-Integration", sirenProperties.getToken());
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<SirenResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, SirenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return sirenMapper.toStructureList(response.getBody());
            } else {
                logger.warn("Réponse non valide pour la requête filtrée : {}", response.getStatusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des établissements filtrés", e);
            return new ArrayList<>();
        }
    }

    private String buildQueryFromFilters(String filtersJson) {
        try {
            JsonNode rootNode = objectMapper.readTree(filtersJson);
            List<String> queryParts = new ArrayList<>();

            if (rootNode.has("raisonSociale")) {
                String value = rootNode.get("raisonSociale").get("value").asText();
                queryParts.add("denominationUniteLegale:\"" + value + "\"");
            }
            if (rootNode.has("pays.id")) {
                JsonNode valuesNode = rootNode.get("pays.id").get("value");
                if (valuesNode.isArray()) {
                    List<String> paysIds = new ArrayList<>();
                    for (JsonNode idNode : valuesNode) {
                        paysIds.add(idNode.asText());
                    }
                    queryParts.add("paysUniteLegale:(" + String.join(" OR ", paysIds) + ")");
                }
            }

            String query = String.join(" AND ", queryParts);
            logger.info("Requête construite depuis les filtres : {}", query);
            return query;
        } catch (Exception e) {
            logger.error("Erreur lors de la construction de la requête à partir des filtres JSON", e);
            return "";
        }
    }

    public List<Structure> getAllEtablissements() {
        String url = sirenProperties.getUrl() + "/siret";
        logger.info("Requête pour récupérer tous les établissements : {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-INSEE-Api-Key-Integration", sirenProperties.getToken());
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<SirenResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, SirenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return sirenMapper.toStructureList(response.getBody());
            } else {
                logger.warn("Réponse non valide pour la récupération de tous les établissements : {}", response.getStatusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de tous les établissements", e);
            return new ArrayList<>();
        }
    }
}
