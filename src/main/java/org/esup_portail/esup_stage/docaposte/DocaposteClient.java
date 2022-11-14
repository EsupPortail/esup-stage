package org.esup_portail.esup_stage.docaposte;

import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.docaposte.gen.*;
import org.esup_portail.esup_stage.model.Convention;
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

    public void upload(Convention convention) {
        String filename = "Convention_" + convention.getId() + "_" + convention.getEtudiant().getPrenom() + "_" + convention.getEtudiant().getNom();
        ByteArrayOutputStream ou = new ByteArrayOutputStream();
        impressionService.generateConventionAvenantPDF(convention, null, ou);
        String otpData = impressionService.generateOtpData(convention);

        DataFileVO documentFile = new DataFileVO();
        documentFile.setDataHandler(ou.toByteArray());
        documentFile.setFilename(filename + ".pdf");
        DataFileVO optDataFile = new DataFileVO();
        optDataFile.setDataHandler(otpData.getBytes());
        optDataFile.setFilename("METAS_" + filename + ".xml");

        OtpUpload request = new OtpUpload();
        request.setSiren(applicationBootstrap.getAppConfig().getDocaposteSiren());
        request.setCircuitId(convention.getCentreGestion().getCircuitSignature());
        request.setDocument(documentFile);
        request.setOtpData(optDataFile);
        request.setLabel("");
        OtpUploadResponse response = ((JAXBElement<OtpUploadResponse>) getWebServiceTemplate().marshalSendAndReceive(new ObjectFactory().createOtpUpload(request))).getValue();

        convention.setDateEnvoiSignature(new Date());
        convention.setDocumentId(response.getReturn().get(0));
        convention.setUrlSignature(response.getReturn().get(1));
        conventionJpaRepository.saveAndFlush(convention);
    }

    public HistoryResponse getHistorique(String documentId) {
        History request = new History();
        request.setDocumentId(documentId);
        return ((JAXBElement<HistoryResponse>) getWebServiceTemplate().marshalSendAndReceive(new ObjectFactory().createHistory(request))).getValue();
    }

}