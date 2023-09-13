package org.esup_portail.esup_stage.docaposte;

import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.docaposte.gen.*;
import org.esup_portail.esup_stage.enums.TypeSignatureEnum;
import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import javax.xml.bind.JAXBElement;
import java.io.ByteArrayOutputStream;
import java.util.Date;

public class DocaposteClient extends WebServiceGatewaySupport {

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @Autowired
    ImpressionService impressionService;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;
    @Autowired
    AvenantJpaRepository avenantJpaRepository;

    public void upload(Convention convention, Avenant avenant) {
        String filename = "";
        String label = "conventions";
        String documentId = convention.getDocumentId();
        if (avenant != null) {
            filename += "Avenant_" + avenant.getId() + "_";
            label = "avenants";
            documentId = avenant.getDocumentId();
        }
        filename += "Convention_" + convention.getId() + "_" + convention.getEtudiant().getPrenom() + "_" + convention.getEtudiant().getNom();
        ByteArrayOutputStream ou = new ByteArrayOutputStream();
        impressionService.generateConventionAvenantPDF(convention, avenant, ou, false);

        DataFileVO documentFile = new DataFileVO();
        documentFile.setDataHandler(ou.toByteArray());
        documentFile.setFilename(filename + ".pdf");

        // Dépôt du PDF dans Docaposte
        if (documentId == null) {
            Upload uploadRequest = new Upload();
            uploadRequest.setSubscriberId(applicationBootstrap.getAppConfig().getDocaposteSiren());
            uploadRequest.setCircuitId(convention.getCentreGestion().getCircuitSignature());
            uploadRequest.setDataFileVO(documentFile);
            uploadRequest.setLabel(label);
            UploadResponse uploadResponse = ((JAXBElement<UploadResponse>) getWebServiceTemplate().marshalSendAndReceive(new ObjectFactory().createUpload(uploadRequest))).getValue();
            documentId = uploadResponse.getReturn();
            if (avenant != null) {
                avenant.setDateEnvoiSignature(new Date());
                avenant.setDocumentId(documentId);
                avenantJpaRepository.saveAndFlush(avenant);
            } else {
                convention.setDateEnvoiSignature(new Date());
                convention.setDocumentId(documentId);
                conventionJpaRepository.saveAndFlush(convention);
            }
        }

        // Ajout des otp pour les signatures otp
        String otpData = impressionService.generateXmlData(convention, TypeSignatureEnum.otp);
        String metaData = impressionService.generateXmlData(convention, TypeSignatureEnum.serveur);
        if (otpData != null) {
            UploadOTPInformation requestUploadOtpInformation = new UploadOTPInformation();
            requestUploadOtpInformation.setDocumentId(documentId);
            requestUploadOtpInformation.setArg1(otpData.getBytes());
            ((JAXBElement<UploadOTPInformationResponse>) getWebServiceTemplate().marshalSendAndReceive(new ObjectFactory().createUploadOTPInformation(requestUploadOtpInformation))).getValue();
        }

        // Ajout des informations meta pour les signatures serveurs
        if (metaData != null) {
            UploadMeta requestUploadMeta = new UploadMeta();
            requestUploadMeta.setDocumentId(documentId);
            requestUploadMeta.setArg1(metaData.getBytes());
            ((JAXBElement<UploadMetaResponse>) getWebServiceTemplate().marshalSendAndReceive(new ObjectFactory().createUploadMeta(requestUploadMeta))).getValue();
        }
    }

    public HistoryResponse getHistorique(String documentId) {
        History request = new History();
        request.setDocumentId(documentId);
        return ((JAXBElement<HistoryResponse>) getWebServiceTemplate().marshalSendAndReceive(new ObjectFactory().createHistory(request))).getValue();
    }

}
