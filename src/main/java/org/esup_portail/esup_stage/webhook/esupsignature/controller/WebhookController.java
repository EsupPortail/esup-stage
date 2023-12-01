package org.esup_portail.esup_stage.webhook.esupsignature.controller;

import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.dto.PdfMetadataDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.webhook.esupsignature.service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class WebhookController {

    @Autowired
    WebhookService webhookService;

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    private final WebClient webClient;

    public WebhookController(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    @PostMapping("/webhook/esup-signature")
    public String upload(@RequestParam(name = "conventionid", required = false) Integer conventionId, @RequestParam(name = "avenantid", required = false) Integer avenantId) {
        if ((conventionId == null && avenantId == null) || (conventionId != null && avenantId != null)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "conventionid OU avenantid obligatoire");
        }
        String token = applicationBootstrap.getAppConfig().getAppPublicTokens()[0];
        String type = "conventions";
        Integer id = conventionId;
        if (avenantId != null) {
            type = "avenants";
            id = avenantId;
        }
        // Récupération du PDF et des metadata
        PdfMetadataDto content = webClient.get()
                .uri(applicationBootstrap.getAppConfig().getLocalApi() + "/public/api/" + type + "/" + id)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .retrieve()
                .bodyToMono(PdfMetadataDto.class)
                .block();
        // Dépôt dans esup-signature
        if (content != null) {
            return webhookService.upload(content);
        }
        return null;
    }
}
