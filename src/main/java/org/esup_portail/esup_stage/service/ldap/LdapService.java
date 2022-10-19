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
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class LdapService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapService.class);

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    private String call(String api, String method, Object params) {
        HttpURLConnection con;
        ObjectMapper mapper = new ObjectMapper();

        try {
            String query = "";
            if (method.equals("GET")) {
                List<String> listParams = new ArrayList<>();
                ((Map<String, String>) params).forEach((key, value) -> listParams.add(key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8)));
                if (listParams.size() > 0) {
                    query += "?" + String.join("&", listParams);
                }
            }
            URL url = new URL(applicationBootstrap.getAppConfig().getReferentielWsLdapUrl() + api + query);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((applicationBootstrap.getAppConfig().getReferentielWsLogin() + ":" + applicationBootstrap.getAppConfig().getReferentielWsPassword()).getBytes()));
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-type", "application/json");
            if (method.equals("POST")) {
                con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = mapper.writeValueAsString(params).getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur code " + con.getResponseCode());
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
            LOGGER.error("Erreur lors de l'appel au ws LDAP " + api + ": " + e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }

    public List<LdapUser> search(String api, LdapSearchDto ldapSearchDto) {
        String response = call(api, "POST", ldapSearchDto);
        try {
            ObjectMapper mapper = new ObjectMapper();
            return Arrays.asList(mapper.readValue(response, LdapUser[].class));
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
                return mapper.readValue(response, LdapUser.class);
            }
            return null;
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur lors de la lecture de la réponse sur l'api bySupannAliasLogin: " + e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }
}
