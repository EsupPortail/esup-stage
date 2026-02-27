package org.esup_portail.esup_stage.service.proprety;

import jakarta.transaction.Transactional;
import org.esup_portail.esup_stage.dto.ConfigTestResultDto;
import org.esup_portail.esup_stage.dto.DocaposteTestRequestDto;
import org.esup_portail.esup_stage.dto.EsupSignatureTestRequestDto;
import org.esup_portail.esup_stage.dto.MailerTestRequestDto;
import org.esup_portail.esup_stage.dto.ReferentielTestRequestDto;
import org.esup_portail.esup_stage.dto.SireneTestRequestDto;
import org.esup_portail.esup_stage.dto.WebhookTestRequestDto;
import jakarta.mail.internet.MimeMessage;
import org.esup_portail.esup_stage.enums.AppPropertySecretsKey;
import org.esup_portail.esup_stage.model.AppProperty;
import org.esup_portail.esup_stage.repository.AppPropertyJpaRepository;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.config.properties.ConfigReloadEvent;
import org.esup_portail.esup_stage.service.sirene.model.SirenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.model.Utilisateur;

@Service
public class AppProperyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppProperyService.class);

    @Autowired
    private AppPropertyJpaRepository appPropertyJpaRepository;

    @Autowired
    private ApogeeService apogeeService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private PropertyCryptoService propertyCryptoService;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<AppProperty> getAll() {
        return appPropertyJpaRepository.findAll();
    }

    public Map<String, String> getOverrides() {
        List<AppProperty> props = appPropertyJpaRepository.findAll();
        Map<String, String> overrides = new LinkedHashMap<>();
        for (AppProperty prop : props) {
            if (prop == null) {
                continue;
            }
            String key = prop.getKey();
            if (!StringUtils.hasText(key)) {
                continue;
            }
            String value = resolveValue(prop);
            if (!StringUtils.hasText(value)) {
                continue;
            }
            overrides.put(key, value);
        }
        return overrides;
    }

    @Transactional
    public void save(String key, String value) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        AppProperty appProperty = appPropertyJpaRepository.findByKey(key);
        if (appProperty == null) {
            LOGGER.warn("Clé de configuration inconnue : {}", key);
            return;
        }

        boolean secret = isSecretKey(key) || Boolean.TRUE.equals(appProperty.getIsSecret());

        if (secret) {
            appProperty.setIsSecret(true);
            if (value == null) {
                appProperty.setUpdatedAt(LocalDateTime.now());
                appPropertyJpaRepository.save(appProperty);
                applicationEventPublisher.publishEvent(new ConfigReloadEvent());
                return;
            }
            if (StringUtils.hasText(value)) {
                appProperty.setValueEncrypted(propertyCryptoService.encrypt(value));
            } else {
                appProperty.setValueEncrypted(null);
            }
            appProperty.setValue(null);
        } else {
            appProperty.setIsSecret(false);
            appProperty.setValue(value);
            appProperty.setValueEncrypted(null);
        }

        appProperty.setUpdatedAt(LocalDateTime.now());
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (utilisateur != null && StringUtils.hasText(utilisateur.getLogin())) {
            appProperty.setUpdatedBy(utilisateur.getLogin());
        }
        appPropertyJpaRepository.save(appProperty);
        applicationEventPublisher.publishEvent(new ConfigReloadEvent());
    }

    private boolean isSecretKey(String key) {
        return AppPropertySecretsKey.isSecret(key);
    }



    private String resolveValue(AppProperty prop) {
        if (prop.getIsSecret()) {
            if (StringUtils.hasText(prop.getValueEncrypted())) {
                return propertyCryptoService.decrypt(prop.getValueEncrypted());
            }
            return null;
        }
        return prop.getValue();
    }

    public ConfigTestResultDto testMailer(MailerTestRequestDto request) {
        if (request == null || !StringUtils.hasText(request.getHost()) || request.getPort() == null) {
            return error("Paramètres SMTP invalides.");
        }

        try {
            JavaMailSenderImpl sender = buildJavaMailSender(request);
            sender.testConnection();

            if (StringUtils.hasText(request.getMailto())) {
                String subject = StringUtils.hasText(request.getSubject()) ? request.getSubject() : "Test SMTP";
                String content = StringUtils.hasText(request.getContent()) ? request.getContent() : "Test SMTP OK.";

                MimeMessage message = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
                helper.setTo(request.getMailto());
                helper.setSubject(subject);
                helper.setText(content, true);

                String from = StringUtils.hasText(request.getUsername()) ? request.getUsername() : request.getMailto();
                if (StringUtils.hasText(from)) {
                    helper.setFrom(from);
                }

                sender.send(message);
                return success("Connexion SMTP OK. Mail de test envoyé.");
            }

            return success("Connexion SMTP OK.");
        } catch (Exception e) {
            return error("Erreur SMTP : " + e.getMessage());
        }
    }

    public ConfigTestResultDto testReferentiel(ReferentielTestRequestDto request) {
        try {
            List<?> composantes = apogeeService.getListComposante();
            if (composantes == null || composantes.isEmpty()) {
                return error("Aucune donnée renvoyée par Apogée.");
            }
            return success("Connexion Apogée OK.");
        } catch (Exception e) {
            return error("Erreur Apogée : " + e.getMessage());
        }
    }

    public ConfigTestResultDto testWebhook(WebhookTestRequestDto request) {
        if (request == null || !StringUtils.hasText(request.getUri())) {
            return error("URI webhook manquante.");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            if (StringUtils.hasText(request.getToken())) {
                headers.set("Authorization", "Bearer " + request.getToken());
            }
            ResponseEntity<String> resp = restTemplate.exchange(
                    request.getUri(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );
            if (resp.getStatusCode().is2xxSuccessful()) {
                return success("Webhook joignable (ping OK).");
            }
            return error("Webhook non joignable : " + resp.getStatusCode());
        } catch (Exception e) {
            return error("Erreur webhook : " + e.getMessage());
        }
    }

    public ConfigTestResultDto testSirene(SireneTestRequestDto request) {
        if (request == null || !StringUtils.hasText(request.getUrl()) || !StringUtils.hasText(request.getToken())) {
            return error("Paramètres Sirène invalides.");
        }
        if (!StringUtils.hasText(request.getSiret())) {
            return error("SIRET de test manquant.");
        }
        try {
            String url = request.getUrl() + "/siret/" + request.getSiret();
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-INSEE-Api-Key-Integration", request.getToken());
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SirenResponse> resp = restTemplate.exchange(url, HttpMethod.GET, entity, SirenResponse.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                return success("API Sirène OK (SIRET trouvé).");
            }
            return error("API Sirène en erreur : " + resp.getStatusCode());
        } catch (Exception e) {
            return error("Erreur Sirène : " + e.getMessage());
        }
    }

    public ConfigTestResultDto testDocaposte(DocaposteTestRequestDto request) {
        if (request == null || !StringUtils.hasText(request.getUri())) {
            return error("URI Docaposte manquante.");
        }
        if (!StringUtils.hasText(request.getSiren())) {
            return error("SIREN Docaposte manquant.");
        }
        if (!StringUtils.hasText(request.getKeystorePath()) || !StringUtils.hasText(request.getKeystorePassword())) {
            return error("Keystore Docaposte manquant.");
        }
        if (!StringUtils.hasText(request.getTruststorePath()) || !StringUtils.hasText(request.getTruststorePassword())) {
            return error("Truststore Docaposte manquant.");
        }
        return pingUrl(request.getUri(), "Docaposte");
    }

    public ConfigTestResultDto testEsupSignature(EsupSignatureTestRequestDto request) {
        if (request == null || !StringUtils.hasText(request.getUri())) {
            return error("URI Esup-Signature manquante.");
        }
        if (!StringUtils.hasText(request.getCircuit())) {
            return error("Circuit Esup-Signature manquant.");
        }
        return pingUrl(request.getUri(), "Esup-Signature");
    }

    private ConfigTestResultDto notImplemented(String message) {
        ConfigTestResultDto dto = new ConfigTestResultDto();
        dto.setResult("error");
        dto.setMessage(message);
        return dto;
    }

    private ConfigTestResultDto success(String message) {
        ConfigTestResultDto dto = new ConfigTestResultDto();
        dto.setResult("success");
        dto.setMessage(message);
        return dto;
    }

    private ConfigTestResultDto error(String message) {
        ConfigTestResultDto dto = new ConfigTestResultDto();
        dto.setResult("error");
        dto.setMessage(message);
        return dto;
    }

    private ConfigTestResultDto pingUrl(String url, String label) {
        try {
            HttpHeaders headers = new HttpHeaders();
            ResponseEntity<String> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );
            if (resp.getStatusCode().is5xxServerError()) {
                return error(label + " indisponible : " + resp.getStatusCode());
            }
            return success(label + " joignable (" + resp.getStatusCode() + ").");
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().is5xxServerError()) {
                return error(label + " indisponible : " + e.getStatusCode());
            }
            return success(label + " joignable (" + e.getStatusCode() + ").");
        } catch (Exception e) {
            return error("Erreur " + label + " : " + e.getMessage());
        }
    }

    private JavaMailSenderImpl buildJavaMailSender(MailerTestRequestDto request) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(request.getHost());
        sender.setPort(request.getPort());

        if ("true".equalsIgnoreCase(request.getAuth())) {
            sender.setUsername(request.getUsername());
            sender.setPassword(request.getPassword());
        }

        Properties props = new Properties();
        String protocol = StringUtils.hasText(request.getProtocol()) ? request.getProtocol() : "smtp";
        props.put("mail.transport.protocol", protocol);
        if ("smtps".equalsIgnoreCase(protocol)) {
            props.put("mail.smtps.auth", String.valueOf("true".equalsIgnoreCase(request.getAuth())));
        } else {
            props.put("mail.smtp.auth", String.valueOf("true".equalsIgnoreCase(request.getAuth())));
        }
        sender.setJavaMailProperties(props);
        return sender;
    }
}
