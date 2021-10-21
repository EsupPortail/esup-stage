package org.esup_portail.esup_stage.service.apogee;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ApogeeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApogeeService.class);

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    private String call(String api, Map<String, String> params) {
        HttpURLConnection con;
        ObjectMapper mapper = new ObjectMapper();

        try {
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
            return mapper.readValue(response, EtudiantRef.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur lors de la lecture de la réponse sur l'api etudiantRef: " + e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }
}
