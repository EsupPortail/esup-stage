package org.esup_portail.esup_stage.service.siren;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.esup_portail.esup_stage.config.properties.SirenProperties;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.repository.StructureJpaRepository;
import org.esup_portail.esup_stage.service.siren.model.ListStructureSirenDTO;
import org.esup_portail.esup_stage.service.siren.model.SirenResponse;
import org.esup_portail.esup_stage.service.siren.utils.SirenMapper;
import org.esup_portail.esup_stage.service.siren.utils.SireneQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SirenService {

    private static final Logger logger = LoggerFactory.getLogger(SirenService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SirenMapper sirenMapper;

    @Autowired
    private SirenProperties sirenProperties;
    @Autowired
    private StructureJpaRepository structureJpaRepository;

    public SirenService() {
        this.restTemplate = new RestTemplate();
    }

    public Structure getEtablissement(String siret) {
        String url = sirenProperties.getUrl() + "/siret/" + siret;
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

    public ListStructureSirenDTO getEtablissementFiltered(int page, int perpage, String filtersJson) {
        String baseUrl = sirenProperties.getUrl() + "/siret";
        String lucene = SireneQueryBuilder.buildLuceneQuery(filtersJson);

        UriComponents components = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .queryParam("q", lucene)
                .queryParam("nombre",  perpage)
                .queryParam("page",  page)
                .build()
                .encode(StandardCharsets.UTF_8);

        String url = components.toUriString().replaceAll("(?<=AND)%20|%20(?=AND)", " ");

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-INSEE-Api-Key-Integration", sirenProperties.getToken());
        headers.set("Accept", "application/json");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<SirenResponse> resp = restTemplate.exchange(url, HttpMethod.GET, entity, SirenResponse.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return new ListStructureSirenDTO(resp.getBody().getHeader().getTotal(), sirenMapper.toStructureList(resp.getBody()));
            }
        } catch (HttpClientErrorException.NotFound ignored) {
            return new ListStructureSirenDTO(0,Collections.emptyList());
        } catch (HttpClientErrorException.BadRequest e) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Erreur lors de la récupération des établissements, vérifiez vos filtres");
        } catch (HttpClientErrorException e) {
            logger.error("Erreur d'authentification lors de la récupération des établissements : {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des établissements : {}", e.getMessage());
        }
        return new ListStructureSirenDTO(0,Collections.emptyList());
    }



    public List<Structure> getAllEtablissements() {
        String url = sirenProperties.getUrl() + "/siret";

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

    public void update(Structure structure) {
        String url = sirenProperties.getUrl() + "/siret/" + structure.getNumeroSiret();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-INSEE-Api-Key-Integration", sirenProperties.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<SirenResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, SirenResponse.class
            );
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                structure = sirenMapper.updateStructure(response.getBody(), structure);
                structureJpaRepository.save(structure);
            } else {
                logger.warn("Erreur lors de la mise à jour de l'établissement {} avec l'id {} : {}", structure.getRaisonSociale(),structure.getId(), response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de l'établissement {}, id : {}", structure.getRaisonSociale(),structure.getId(), e);
        }
    }
}
