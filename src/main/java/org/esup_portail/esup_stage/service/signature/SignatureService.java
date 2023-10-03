package org.esup_portail.esup_stage.service.signature;

import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.docaposte.DocaposteClient;
import org.esup_portail.esup_stage.dto.IdsListDto;
import org.esup_portail.esup_stage.dto.ResponseDto;
import org.esup_portail.esup_stage.enums.AppSignatureEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.service.ConventionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Date;

@Service
public class SignatureService {

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    AvenantJpaRepository avenantJpaRepository;

    @Autowired
    ConventionService conventionService;

    @Autowired
    DocaposteClient docaposteClient;

    public int upload(IdsListDto idsListDto, boolean isAvenant) {
        AppSignatureEnum appSignature = applicationBootstrap.getAppConfig().getAppSignatureEnabled();
        if (appSignature == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La signature électronique n'est pas configurée");
        }
        if (idsListDto.getIds().size() == 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "La liste est vide");
        }
        int count = 0;
        for (int id : idsListDto.getIds()) {
            Convention convention = null;
            String queryParam = "conventionid";
            if (isAvenant) {
                queryParam = "avenantid";
                Avenant avenant = avenantJpaRepository.findById(id);
                if (avenant != null) convention = avenant.getConvention();
            } else {
                convention = conventionJpaRepository.findById(id);
            }
            if (convention == null || (appSignature == AppSignatureEnum.DOCAPOSTE && convention.getCentreGestion().getCircuitSignature() == null)) {
                continue;
            }
            ResponseDto controles = conventionService.controleEmailTelephone(convention);
            if (controles.getError().size() > 0) {
                continue;
            }
            switch (appSignature) {
                case DOCAPOSTE:
                    docaposteClient.upload(convention, null);
                    break;
                case ESUPSIGNATURE:
                case EXTERNE:
                    WebClient client = WebClient.create();
                    String documentId = client.post()
                            .uri(applicationBootstrap.getAppConfig().getWebhookSignatureUri() + "?" + queryParam + "=" + id)
                            .header("Authorization", "Bearer " + applicationBootstrap.getAppConfig().getWebhookSignatureToken())
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    if (documentId != null) {
                        convention.setDateEnvoiSignature(new Date());
                        convention.setDocumentId(documentId);
                        conventionJpaRepository.saveAndFlush(convention);
                    }
                    break;
            }
            count++;
        }
        return count;
    }
}
