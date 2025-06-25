package org.esup_portail.esup_stage.webhook.esupsignature.service;

import com.itextpdf.commons.utils.Base64;
import lombok.extern.slf4j.Slf4j;
import org.esup_portail.esup_stage.config.properties.SignatureProperties;
import org.esup_portail.esup_stage.dto.MetadataDto;
import org.esup_portail.esup_stage.dto.PdfMetadataDto;
import org.esup_portail.esup_stage.enums.SignataireEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CentreGestionSignataire;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.service.signature.model.Historique;
import org.esup_portail.esup_stage.webhook.esupsignature.service.model.*;
import org.esup_portail.esup_stage.webhook.esupsignature.service.model.Steps;
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
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WebhookService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookService.class);
    private final WebClient webClient;
    @Autowired
    SignatureProperties signatureProperties;

    private final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    public WebhookService(WebClient.Builder builder) {
        this.webClient = builder.build();
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

    public String upload(PdfMetadataDto content) {
        logger.debug("Upload dans esup-signature de la convention ou de l'avenant : {}",content.getMetadata().getTitle());
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

        logger.debug(" - WorkflowId : {} ", content.getMetadata().getWorkflowId());
        logger.debug(" - Pdf : {} ", content.getPdf64() != null);
        for(WorkflowStep step : workflowSteps) {
            logger.debug(" - Step {} : {}", step.getStepNumber(), step.getRecipients().stream().map(Recipient::getEmail).collect(Collectors.joining(",")));
        }

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("multipartFiles", geMultipartFile(metadataDto.getTitle(), Base64.decode(content.getPdf64())));
        builder.part("createByEppn", "system");
        builder.part("recipientsEmails", String.join(",", recipients));
        builder.part("stepsJsonString", workflowSteps);

        String result = webClient.post()
                .uri(signatureProperties.getEsupsignature().getUri() + "/workflows/" + metadataDto.getWorkflowId() + "/new")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        logger.debug(" - Retour de l'application de signature : {} ", result);
         return result;
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
            for (int ordre = 1; ordre <= response.getAuditSteps().size(); ++ordre) {
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

    public List<Historique> getHistoriqueStatus(String documentId, Convention convention) {
        List<Historique> historiques = new ArrayList<>();

        List<Steps> steps = Objects.requireNonNull(webClient.get()
                .uri(signatureProperties.getEsupsignature().getUri() + "/signrequests/" + documentId + "/steps")
                .retrieve()
                .bodyToFlux(Steps.class)
                .collectList()
                .block());

        if (steps != null) {
            List<CentreGestionSignataire> signataires = convention.getCentreGestion().getSignataires();
            for (int ordre = 1; ordre <= steps.size(); ++ordre) {
                Steps step = steps.get(ordre - 1);
                int finalOrdre = ordre;
                CentreGestionSignataire signataire = signataires.stream()
                        .filter(s -> s.getOrdre() == finalOrdre)
                        .findAny()
                        .orElse(null);

                for (Steps.RecipientAction recipientAction : step.getRecipientsActions()) {
                    if ( signataire != null && Objects.equals(recipientAction.getActionType(), "signed")) {
                        Historique historique = new Historique();
                        historique.setDateDepot(
                                recipientAction.getActionDate() != null ? recipientAction.getActionDate() : null
                        );
                        historique.setDateSignature(
                                recipientAction.getActionDate() != null ? recipientAction.getActionDate() : null
                        );
                        historique.setTypeSignataire(signataire.getId().getSignataire());
                        if (historique.getDateDepot() != null && historique.getDateSignature() != null && historique.getTypeSignataire() != null) {
                            historiques.add(historique);
                        }
                    }
                }
            }
        }

        return historiques;
    }

}
