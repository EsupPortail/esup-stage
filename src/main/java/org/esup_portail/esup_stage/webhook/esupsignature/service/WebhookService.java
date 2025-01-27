package org.esup_portail.esup_stage.webhook.esupsignature.service;

import com.itextpdf.commons.utils.Base64;
import org.esup_portail.esup_stage.config.properties.SignatureProperties;
import org.esup_portail.esup_stage.dto.MetadataDto;
import org.esup_portail.esup_stage.dto.PdfMetadataDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CentreGestionSignataire;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.service.signature.model.Historique;
import org.esup_portail.esup_stage.webhook.esupsignature.service.model.AuditStep;
import org.esup_portail.esup_stage.webhook.esupsignature.service.model.AuditTrail;
import org.esup_portail.esup_stage.webhook.esupsignature.service.model.Recipient;
import org.esup_portail.esup_stage.webhook.esupsignature.service.model.WorkflowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WebhookService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookService.class);

    @Autowired
    SignatureProperties signatureProperties;

    private final WebClient webClient;

    public WebhookService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public String upload(PdfMetadataDto content) {
        MetadataDto metadataDto = content.getMetadata();
        List<String> recipients = metadataDto.getSignatory().stream().map(s -> s.getOrder() + "*" + s.getMail()).collect(Collectors.toList());
        List<WorkflowStep> workflowSteps = new ArrayList<>();
        metadataDto.getSignatory().forEach(s -> {
            WorkflowStep step = workflowSteps.stream().filter(ws -> ws.getStepNumber() == s.getOrder()).findFirst().orElse(null);
            if (step == null) {
                step = new WorkflowStep();
                step.setStepNumber(s.getOrder());
                workflowSteps.add(step);
            }
            Recipient recipient = new Recipient();
            recipient.setStep(s.getOrder());
            recipient.setEmail(s.getMail());
            recipient.setFirstName(s.getGivenname());
            recipient.setName(s.getName());
//            recipient.setPhone(s.getPhone());
            step.getRecipients().add(recipient);
        });


        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("multipartFiles", geMultipartFile(metadataDto.getTitle(), Base64.decode(content.getPdf64())));
        builder.part("createByEppn", "system");
        builder.part("recipientsEmails", String.join(",", recipients));
        builder.part("stepsJsonString", workflowSteps);

        return webClient.post()
                .uri(signatureProperties.getEsupsignature().getUri() + "/workflows/" + metadataDto.getWorkflowId() + "/new")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public List<Historique> getHistorique(String documentId, Convention convention) {
        AuditTrail response = webClient.get()
                .uri(signatureProperties.getEsupsignature().getUri() + "/signrequests/audit-trail/" + documentId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AuditTrail.class)
                .block();

        List<Historique> historiques = new ArrayList<>();
        if (response != null) {
            List<CentreGestionSignataire> signataires = convention.getCentreGestion().getSignataires();
            for (int ordre = 1 ; ordre <= response.getAuditSteps().size() ; ++ordre) {
                AuditStep step = response.getAuditSteps().get(ordre - 1);
                int finalOrdre = ordre;
                CentreGestionSignataire signataire = signataires.stream().filter(s -> s.getOrdre() == finalOrdre).findAny().orElse(null);
                if (signataire != null) {
                    Historique historique = new Historique();
                    historique.setDateDepot(step.getTimeStampDate());
                    historique.setDateSignature(step.getTimeStampDate());
                    historique.setTypeSignataire(signataire.getId().getSignataire());
                    historiques.add(historique);
                }
            }
        }
        return historiques;
    }

    public InputStream download(String documentId) {
        return webClient.get()
                .uri(signatureProperties.getEsupsignature().getUri() + "/signrequests/get-last-file/" + documentId)
                .accept(MediaType.APPLICATION_PDF)
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(InputStreamResource.class))
                .map(inputStreamResource -> {
                    try {
                        return inputStreamResource.getInputStream();
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la récupération du document signé");
                    }
                })
                .block();
    }

    private static Resource geMultipartFile(String filename, byte[] bytes) {
        try {
            Path tmpFile = Files.createTempFile(filename, ".pdf");
            Files.write(tmpFile, bytes);
            return new FileSystemResource(tmpFile.toFile());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
    }
}
