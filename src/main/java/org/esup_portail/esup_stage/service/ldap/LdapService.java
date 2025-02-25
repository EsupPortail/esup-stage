package org.esup_portail.esup_stage.service.ldap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.esup_portail.esup_stage.config.properties.ReferentielProperties;
import org.esup_portail.esup_stage.dto.LdapSearchDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@Slf4j
public class LdapService {

    private final WebClient webClient;
    @Autowired
    private ReferentielProperties referentielProperties;

    public LdapService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    private String call(String api, String method, Object params) {
        if (referentielProperties.getLogin() == null || referentielProperties.getPassword() == null) {
            log.error("Erreur lors de l'appel au ws LDAP {} : login ou mot de passe non renseigné",api);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
        try {
            if (method.equals("GET")) {
                return webClient.get()
                        .uri(referentielProperties.getLdapUrl() + api, uri -> {
                            ((Map<String, String>) params).forEach(uri::queryParam);
                            return uri.build();
                        })
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((referentielProperties.getLogin() + ":" + referentielProperties.getPassword()).getBytes()))
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } else {
                return webClient.post()
                        .uri(referentielProperties.getLdapUrl() + api)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((referentielProperties.getLogin() + ":" + referentielProperties.getPassword()).getBytes()))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(params))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            }
        } catch (Exception e) {
            log.error("context",e);
            log.error("Erreur lors de l'appel au ws LDAP {}", api);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }

    public List<LdapUser> search(String api, LdapSearchDto ldapSearchDto) {
        String response = call(api, "POST", ldapSearchDto);
        try {
            log.info("LDAP {} parametres: {}",api, ldapSearchDto);
            ObjectMapper mapper = new ObjectMapper();
            List<LdapUser> users = Arrays.asList(mapper.readValue(response, LdapUser[].class));
            log.info("{} utilisateurs trouvé",users.size());
            return users;
        } catch (JsonProcessingException e) {
            log.error("context",e);
            log.error("Erreur lors de la lecture de la réponse sur l'api {}", api);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }

    public LdapUser searchByLogin(String login) {
        if (login == null || login.isEmpty()) {
            log.error("Erreur lors de la recherche de l'utilisateur par login: login est null ou vide");
            return null;
        }
        Map<String, String> params = new HashMap<>();
        params.put("login", login);
        String response = call("/bySupannAliasLogin", "GET", params);
        try {
            ObjectMapper mapper = new ObjectMapper();
            if (!response.isEmpty()) {
                log.info("Utilisateur login = {} trouvé", login);
                return mapper.readValue(response, LdapUser.class);
            } else {
                log.info("Utilisateur login = {} non trouvé", login);
            }
            return null;
        } catch (JsonProcessingException e) {
            log.error("Erreur lors de la lecture de la réponse sur l'api bySupannAliasLogin", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }
}
