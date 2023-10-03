package org.esup_portail.esup_stage.webhook.esupsignature.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.commons.utils.Base64;
import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.dto.MetadataDto;
import org.esup_portail.esup_stage.dto.PdfMetadataDto;
import org.esup_portail.esup_stage.enums.SignataireEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.service.signature.model.Historique;
import org.esup_portail.esup_stage.webhook.esupsignature.service.model.AuditStep;
import org.esup_portail.esup_stage.webhook.esupsignature.service.model.AuditTrail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WebhookService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookService.class);

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    public String upload(PdfMetadataDto content) {
        MetadataDto metadataDto = content.getMetadata();
        List<String> recipients = metadataDto.getSignatory().stream().map(s -> s.getOrder() + "*" + s.getMail()).collect(Collectors.toList());

        WebClient client = WebClient.create();
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("multipartFiles", geMultipartFile(metadataDto.getTitle(), Base64.decode(content.getPdf64())));
        builder.part("createByEppn", "system");
        builder.part("recipientsEmails", recipients);

        return client.post()
                .uri(applicationBootstrap.getAppConfig().getEsupSignatureUri() + "/workflows/" + applicationBootstrap.getAppConfig().getEsupSignatureCiruit() + "/new")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public List<Historique> getHistorique(String documentId, Convention convention) {
        WebClient client = WebClient.create();
        String response = client.get()
                .uri(applicationBootstrap.getAppConfig().getEsupSignatureUri() + "/signrequests/audit-trail/" + documentId)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper mapper = new ObjectMapper();
            AuditTrail auditTrail = mapper.readValue(response, AuditTrail.class);

            List<Historique> historiques = new ArrayList<>();
            for (AuditStep step : auditTrail.getAuditSteps()) {
                Historique historique = new Historique();
                historique.setDateDepot(new Date()); // TODO ?
                historique.setDateSignature(step.getTimeStampDate());
                if (step.getEmail().equals(convention.getEtudiant().getMail())) {
                    historique.setTypeSignataire(SignataireEnum.etudiant);
                } else if (step.getEmail().equals(convention.getEnseignant().getMail())) {
                    historique.setTypeSignataire(SignataireEnum.enseignant);
                } else if (step.getEmail().equals(convention.getContact().getMail())) {
                    historique.setTypeSignataire(SignataireEnum.tuteur);
                } else if (step.getEmail().equals(convention.getSignataire().getMail())) {
                    historique.setTypeSignataire(SignataireEnum.signataire);
                } else if (step.getEmail().equals(convention.getCentreGestion().getMail())) {
                    historique.setTypeSignataire(SignataireEnum.viseur);
                } else {
                    continue;
                }
                historiques.add(historique);
            }
            return historiques;
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue.");
        }
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
