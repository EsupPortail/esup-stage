package org.esup_portail.esup_stage.service.sirene;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.esup_portail.esup_stage.config.properties.SireneProperties;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.repository.StructureJpaRepository;
import org.esup_portail.esup_stage.service.sirene.model.ListStructureSireneDTO;
import org.esup_portail.esup_stage.service.sirene.model.SirenResponse;
import org.esup_portail.esup_stage.service.sirene.utils.SireneMapper;
import org.esup_portail.esup_stage.service.sirene.utils.SireneQueryBuilder;
import org.esup_portail.esup_stage.events.StructureUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SireneService {

    private static final Logger logger = LoggerFactory.getLogger(SireneService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SireneMapper sirenMapper;

    @Autowired
    private SireneProperties sireneProperties;

    @Autowired
    private StructureJpaRepository structureJpaRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;


    public SireneService() {
        this.restTemplate = new RestTemplate();
    }

    public Structure getEtablissement(String siret) {
        String url = sireneProperties.getUrl() + "/siret/" + siret;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-INSEE-Api-Key-Integration", sireneProperties.getToken());
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

    public ListStructureSireneDTO getEtablissementFiltered(int page, int perpage, String filtersJson) {
        String baseUrl = sireneProperties.getUrl() + "/siret";
        String lucene = SireneQueryBuilder.buildLuceneQuery(filtersJson);

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("q", lucene)
                .queryParam("nombre", perpage)
                .queryParam("page", page)
                .build(false)
                .toUri();

        logger.debug("url : " + uri);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-INSEE-Api-Key-Integration", sireneProperties.getToken());
        headers.set("Accept", "application/json");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<SirenResponse> resp = restTemplate.exchange(uri, HttpMethod.GET, entity, SirenResponse.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return new ListStructureSireneDTO(resp.getBody().getHeader().getTotal(), sirenMapper.toStructureList(resp.getBody()));
            }
        } catch (HttpClientErrorException.NotFound ignored) {
            return new ListStructureSireneDTO(0,Collections.emptyList());
        } catch (HttpClientErrorException.BadRequest e) {
            logger.error("Erreur de requête de la requete '{}', retour de l'api : {}", uri ,e.getMessage());
            throw new AppException(HttpStatus.BAD_REQUEST, "Erreur lors de la récupération des établissements, vérifiez vos filtres");
        } catch (HttpClientErrorException e) {
            logger.error("Erreur d'authentification lors de la récupération des établissements : {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des établissements : {}", e.getMessage());
        }
        return new ListStructureSireneDTO(0,Collections.emptyList());
    }



    public List<Structure> getAllEtablissements() {
        String url = sireneProperties.getUrl() + "/siret";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-INSEE-Api-Key-Integration", sireneProperties.getToken());
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

    public void update(String oldStructureJson, Structure structure) {
        String url = sireneProperties.getUrl() + "/siret/" + structure.getNumeroSiret();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-INSEE-Api-Key-Integration", sireneProperties.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<SirenResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, SirenResponse.class
            );
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                structure = sirenMapper.updateStructure(response.getBody(), structure);
                structure.setTemSiren(true);
                structureJpaRepository.save(structure);
                eventPublisher.publishEvent(new StructureUpdatedEvent(structure,oldStructureJson,objectMapper.writeValueAsString(structure),true));
            } else {
                logger.warn("Erreur lors de la mise à jour de l'établissement {} avec l'id {} : {}", structure.getRaisonSociale(),structure.getId(), response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de l'établissement {}, id : {}", structure.getRaisonSociale(),structure.getId(), e);
        }
    }
}
