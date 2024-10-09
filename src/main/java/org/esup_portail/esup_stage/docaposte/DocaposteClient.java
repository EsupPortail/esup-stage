package org.esup_portail.esup_stage.docaposte;

import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.docaposte.gen.*;
import org.esup_portail.esup_stage.enums.SignataireEnum;
import org.esup_portail.esup_stage.enums.TypeSignatureEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.model.CentreGestionSignataire;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.esup_portail.esup_stage.service.signature.model.Historique;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import jakarta.xml.bind.JAXBElement;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
        String filename = "Convention_" + convention.getId() + "_" + convention.getEtudiant().getPrenom() + "_" + convention.getEtudiant().getNom();
        String label = "conventions";
        String documentId = convention.getDocumentId();
        if (avenant != null) {
            filename = "Avenant_" + avenant.getId() + "_" + avenant.getConvention().getEtudiant().getPrenom() + "_" + avenant.getConvention().getEtudiant().getNom();
            label = "avenants";
            documentId = avenant.getDocumentId();
        }
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

        String otpData = impressionService.generateXmlData(convention, TypeSignatureEnum.otp);
        String metaData = impressionService.generateXmlData(convention, TypeSignatureEnum.serveur);

        // Ajout des informations meta pour les signatures serveurs
        if (metaData != null) {
            UploadMeta requestUploadMeta = new UploadMeta();
            requestUploadMeta.setDocumentId(documentId);
            requestUploadMeta.setArg1(metaData.getBytes());
            ((JAXBElement<UploadMetaResponse>) getWebServiceTemplate().marshalSendAndReceive(new ObjectFactory().createUploadMeta(requestUploadMeta))).getValue();
        }
        // Ajout des otp pour les signatures otp
        if (otpData != null) {
            UploadOTPInformation requestUploadOtpInformation = new UploadOTPInformation();
            requestUploadOtpInformation.setDocumentId(documentId);
            requestUploadOtpInformation.setArg1(otpData.getBytes());
            ((JAXBElement<UploadOTPInformationResponse>) getWebServiceTemplate().marshalSendAndReceive(new ObjectFactory().createUploadOTPInformation(requestUploadOtpInformation))).getValue();
        }
    }

    public List<Historique> getHistorique(String documentId, List<CentreGestionSignataire> profils) {
        History request = new History();
        request.setDocumentId(documentId);
        HistoryResponse response = ((JAXBElement<HistoryResponse>) getWebServiceTemplate().marshalSendAndReceive(new ObjectFactory().createHistory(request))).getValue();

        List<CentreGestionSignataire> profilsOtp = profils.stream().filter(p -> p.getType() == TypeSignatureEnum.otp).collect(Collectors.toList());
        List<CentreGestionSignataire> profilsServeur = profils.stream().filter(p -> p.getType() == TypeSignatureEnum.serveur).collect(Collectors.toList());
        List<Historique> historiques = new ArrayList<>();
        int ordreOtp = 0;
        int ordreServeur = 0;
        TypeSignatureEnum typeSignature = null;
        for (HistoryEntryVO history : response.getReturn()) {
            boolean signature = false;
            boolean aTraiter = false;
            Date date = history.getDate().toGregorianCalendar().getTime();
            switch (history.getStateName()) {
                case "Informations OTP définies":
                    ordreOtp++;
                    typeSignature = TypeSignatureEnum.otp;
                    aTraiter = true;
                    break;
                case "Envoyé pour signature":
                    ordreServeur++;
                    typeSignature = TypeSignatureEnum.serveur;
                    aTraiter = true;
                    break;
                case "Signé":
                    signature = true;
                    aTraiter = true;
                    break;
            }
            if (aTraiter && typeSignature != null && (ordreOtp > 0 || ordreServeur > 0)) {
                switch (typeSignature) {
                    case otp:
                        setModelHistorique(historiques, profilsOtp.get(ordreOtp - 1), date, signature);
                        break;
                    case serveur:
                        setModelHistorique(historiques, profilsServeur.get(ordreServeur - 1), date, signature);
                        break;
                }
            }
        }
        return historiques;
    }

    public InputStream download(String documentId) {
        Download request = new Download();
        request.setDocumentId(documentId);
        DownloadResponse response = ((JAXBElement<DownloadResponse>) getWebServiceTemplate().marshalSendAndReceive(new ObjectFactory().createDownload(request))).getValue();
        if (response.getReturn() != null && response.getReturn().getContent() != null) {
            try {
                return response.getReturn().getContent().getInputStream();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'appel api de récupération du document");
            }
        }
        return null;
    }

    private void setModelHistorique(List<Historique> historiques, CentreGestionSignataire profil, Date date, boolean signature) {
        Historique historique = historiques.stream().filter(h -> h.getTypeSignataire() == profil.getId().getSignataire()).findAny().orElse(new Historique());
        if (historique.getTypeSignataire() == null) {
            historique.setTypeSignataire(profil.getId().getSignataire());
            historiques.add(historique);
        }
        if (signature) {
            historique.setDateSignature(date);
        } else {
            historique.setDateDepot(date);
        }
    }

}
