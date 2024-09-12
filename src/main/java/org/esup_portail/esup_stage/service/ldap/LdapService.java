package org.esup_portail.esup_stage.service.ldap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class LdapService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapService.class);

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    private final WebClient webClient;

    public LdapService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    private String call(String api, String method, Object params) {
        try {
            if (method.equals("GET")) {
                return webClient.get()
                        .uri(applicationBootstrap.getAppConfig().getReferentielWsLdapUrl() + api, uri -> {
                            ((Map<String, String>) params).forEach(uri::queryParam);
                             return uri.build();
                        })
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((applicationBootstrap.getAppConfig().getReferentielWsLogin() + ":" + applicationBootstrap.getAppConfig().getReferentielWsPassword()).getBytes()))
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } else {
                return webClient.post()
                        .uri(applicationBootstrap.getAppConfig().getReferentielWsLdapUrl() + api)
                        .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((applicationBootstrap.getAppConfig().getReferentielWsLogin() + ":" + applicationBootstrap.getAppConfig().getReferentielWsPassword()).getBytes()))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(params))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            }
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'appel au ws LDAP " + api + ": " + e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }

    public List<LdapUser> search(String api, LdapSearchDto ldapSearchDto) {
        String response = call(api, "POST", ldapSearchDto);
        try {
            LOGGER.info("LDAP " + api + " parametres: " + ldapSearchDto);
            ObjectMapper mapper = new ObjectMapper();
            List<LdapUser> users = Arrays.asList(mapper.readValue(response, LdapUser[].class));
            LOGGER.info(users.size() + " utilisateurs trouvé");
            return users;
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur lors de la lecture de la réponse sur l'api " + api + ": " + e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }

    public LdapUser searchByLogin(String login) {
        Map<String, String> params = new HashMap<>();
        params.put("login", login);
        String response = call("/bySupannAliasLogin", "GET", params);
        try {
            ObjectMapper mapper = new ObjectMapper();
            if (!response.isEmpty()) {
                LOGGER.info("Utilisateur login = " + login + " trouvé");
                return mapper.readValue(response, LdapUser.class);
            } else {
                LOGGER.info("Utilisateur login = " + login + " non trouvé");
            }
            return null;
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur lors de la lecture de la réponse sur l'api bySupannAliasLogin: " + e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }
}
