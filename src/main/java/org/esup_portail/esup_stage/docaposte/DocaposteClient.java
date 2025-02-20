package org.esup_portail.esup_stage.docaposte;


import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.config.properties.SignatureProperties;
import org.esup_portail.esup_stage.docaposte.gen.*;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpsUrlConnectionMessageSender;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@ConditionalOnProperty(prefix = "docaposte", name = "uri")
public class DocaposteClient extends WebServiceGatewaySupport {

    private static final Logger logger = LogManager.getLogger(DocaposteClient.class);
    @Autowired
    private SignatureProperties signatureProperties;
    @Autowired
    private AppliProperties appliProperties;
    @Autowired
    private ImpressionService impressionService;
    @Autowired
    private ConventionJpaRepository conventionJpaRepository;
    @Autowired
    private AvenantJpaRepository avenantJpaRepository;

    public DocaposteClient(SignatureProperties signatureProperties) {
        this.signatureProperties = signatureProperties;
    }

    @PostConstruct
    public void init() {
        try {
            this.getWebServiceTemplate().setMessageSender(createWebServiceMessageSender());
            logger.info("DocaposteClient initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize DocaposteClient", e);
            throw new RuntimeException("Failed to initialize DocaposteClient", e);
        }
    }

    private WebServiceMessageSender createWebServiceMessageSender() {
        try {
            // Keystore initialization
            KeyStore ks = loadKeyStore();
            KeyManagerFactory keyManagerFactory = initializeKeyManagerFactory(ks);

            // Truststore initialization
            KeyStore ts = loadTrustStore();
            TrustManagerFactory trustManagerFactory = initializeTrustManagerFactory(ts);

            // Create and configure message sender
            return createHttpsMessageSender(keyManagerFactory, trustManagerFactory);
        } catch (Exception e) {
            logger.error("Failed to create WebServiceMessageSender", e);
            throw new RuntimeException("Failed to create WebServiceMessageSender", e);
        }
    }

    private KeyStore loadKeyStore() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        logger.debug("Loading keystore from: {}", signatureProperties.getDocaposte().getKeystorePath());
        try (InputStream is = new FileInputStream(signatureProperties.getDocaposte().getKeystorePath())) {
            ks.load(is, signatureProperties.getDocaposte().getKeystorePassword().toCharArray());
            logger.info("Keystore loaded successfully");
            return ks;
        }
    }

    private KeyStore loadTrustStore() throws Exception {
        KeyStore ts = KeyStore.getInstance("JKS");
        logger.debug("Loading truststore from: {}", signatureProperties.getDocaposte().getTruststorePassword());
        try (InputStream is = new FileInputStream(signatureProperties.getDocaposte().getTruststorePassword())) {
            ts.load(is, signatureProperties.getDocaposte().getTruststorePassword().toCharArray());
            logger.info("Truststore loaded successfully");
            return ts;
        }
    }

    private KeyManagerFactory initializeKeyManagerFactory(KeyStore ks) throws Exception {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(ks, signatureProperties.getDocaposte().getKeystorePassword().toCharArray());
        return keyManagerFactory;
    }

    private TrustManagerFactory initializeTrustManagerFactory(KeyStore ts) throws Exception {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(ts);
        return trustManagerFactory;
    }

    private WebServiceMessageSender createHttpsMessageSender(KeyManagerFactory keyManagerFactory, TrustManagerFactory trustManagerFactory) {
        HttpsUrlConnectionMessageSender webServiceMessageSender = new HttpsUrlConnectionMessageSender();
        webServiceMessageSender.setKeyManagers(keyManagerFactory.getKeyManagers());
        webServiceMessageSender.setTrustManagers(trustManagerFactory.getTrustManagers());
        return webServiceMessageSender;
    }


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
        Upload uploadRequest = new Upload();
        uploadRequest.setSubscriberId(signatureProperties.getDocaposte().getSiren());
        uploadRequest.setCircuitId(convention.getCentreGestion().getCircuitSignature());
        uploadRequest.setDataFileVO(documentFile);
        uploadRequest.setLabel(label);
        if (convention.getCentreGestion().isEnvoiDocumentSigne()) {
            String receiver = null;
            if (convention.getSignataire() != null && Strings.isNotEmpty(convention.getSignataire().getMail())) {
                receiver = convention.getSignataire().getMail();
            }
            if (appliProperties.getMailer().getDeliveryAddress() != null && !appliProperties.getMailer().getDeliveryAddress().isEmpty()) {
                String originalReceiver = receiver;
                receiver = appliProperties.getMailer().getDeliveryAddress();
                logger.info("Email destinataire " + originalReceiver + " redirigé vers " + receiver);
            }
            uploadRequest.setEmailDestinataire(receiver);
        }
        try {
            UploadResponse uploadResponse = ((JAXBElement<UploadResponse>) getWebServiceTemplate().marshalSendAndReceive(new ObjectFactory().createUpload(uploadRequest))).getValue();
            documentId = uploadResponse.getReturn();
        } catch (SoapFaultClientException e) {
            if (e.getMessage() != null && e.getMessage().contains("Fichier déjà depose par un agent")) {
                throw new AppException(HttpStatus.BAD_REQUEST, e.getMessage() + " Veuillez le supprimer manuellement");
            } else {
                throw e;
            }
        }
        if (avenant != null) {
            avenant.setDateEnvoiSignature(new Date());
            avenant.setDocumentId(documentId);
            avenantJpaRepository.saveAndFlush(avenant);
        } else {
            convention.setDateEnvoiSignature(new Date());
            convention.setDocumentId(documentId);
            conventionJpaRepository.saveAndFlush(convention);
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

        List<CentreGestionSignataire> profilsOtp = profils.stream().filter(p -> p.getType() == TypeSignatureEnum.otp).toList();
        List<CentreGestionSignataire> profilsServeur = profils.stream().filter(p -> p.getType() == TypeSignatureEnum.serveur).toList();
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
