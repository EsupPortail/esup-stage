package org.esup_portail.esup_stage.webhook.esupsignature.controller;

import lombok.extern.slf4j.Slf4j;
import org.esup_portail.esup_stage.config.PublicSecurityConfiguration;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.MetadataSignataireDto;
import org.esup_portail.esup_stage.dto.PdfMetadataDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.webhook.esupsignature.service.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@RestController
public class WebhookController {

    private final WebClient webClient;
    private final WebhookService webhookService;
    private final AppliProperties appliProperties;
    private Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    public WebhookController(WebClient.Builder builder, WebhookService webhookService, AppliProperties appliProperties) {
        this.webClient = builder.build();
        this.webhookService = webhookService;
        this.appliProperties = appliProperties;
    }

    @PostMapping("/webhook/esup-signature")
    public String upload(@RequestParam(name = "conventionid", required = false) Integer conventionId, @RequestParam(name = "avenantid", required = false) Integer avenantId) {
        logger.info("Upload dans esup-signature de la convention ou de l'avenant avec conventionId={} et avenantId={}", conventionId, avenantId);
        if ((conventionId == null && avenantId == null) || (conventionId != null && avenantId != null)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "conventionid OU avenantid obligatoire");
        }
        String token = appliProperties.getPublicTokens()[0];
        String type = "conventions";
        Integer id = conventionId;
        if (avenantId != null) {
            type = "avenants";
            id = avenantId;
        }
        // Récupération du PDF et des metadata
        PdfMetadataDto content = webClient.get()
                .uri(appliProperties.getLocalApi() + PublicSecurityConfiguration.PATH_FILTER + "/api/" + type + "/" + id)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .retrieve()
                .bodyToMono(PdfMetadataDto.class)
                .block();
        logger.info("Récupération du PDF et des metadata pour l'upload dans esup-signature : {}", content.getMetadata().getSignatory().stream().map(MetadataSignataireDto::getMail).reduce((s1, s2)->s1+","+s2).orElse("aucun signataire"));
        // Dépôt dans esup-signature
        if (content != null) {
            return webhookService.upload(content);
        }
        return null;
    }
}
