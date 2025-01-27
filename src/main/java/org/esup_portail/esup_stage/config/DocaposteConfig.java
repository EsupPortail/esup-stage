package org.esup_portail.esup_stage.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.config.properties.SignatureProperties;
import org.esup_portail.esup_stage.docaposte.DocaposteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpsUrlConnectionMessageSender;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

@Configuration
public class DocaposteConfig {

    @Autowired
    private SignatureProperties signatureProperties;

    private static final Logger logger = LogManager.getLogger(DocaposteConfig.class);

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("org.esup_portail.esup_stage.docaposte.gen");
        return marshaller;
    }

    @Bean
    public DocaposteClient docaposteClient(Jaxb2Marshaller marshaller) {
        DocaposteClient client = new DocaposteClient();
        client.setDefaultUri(signatureProperties.getDocaposte().getUri());
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        client.getWebServiceTemplate().setMessageSender(createWebServiceMessageSender());
        return client;
    }

    private WebServiceMessageSender createWebServiceMessageSender() {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            logger.info("Loaded keystore: " + signatureProperties.getDocaposte().getKeystorePath());
            try (InputStream is = new FileInputStream(signatureProperties.getDocaposte().getKeystorePath())) {
                ks.load(is, signatureProperties.getDocaposte().getKeystorePassword().toCharArray());
            }
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(ks, signatureProperties.getDocaposte().getKeystorePassword().toCharArray());

            KeyStore ts = KeyStore.getInstance("JKS");
            logger.info("Loaded truststore: " + signatureProperties.getDocaposte().getTruststorePassword());
            try (InputStream is = new FileInputStream(signatureProperties.getDocaposte().getTruststorePassword())) {
                ts.load(is, signatureProperties.getDocaposte().getTruststorePassword().toCharArray());
            }
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(ts);

            HttpsUrlConnectionMessageSender webServiceMessageSender = new HttpsUrlConnectionMessageSender();
            webServiceMessageSender.setKeyManagers(keyManagerFactory.getKeyManagers());
            webServiceMessageSender.setTrustManagers(trustManagerFactory.getTrustManagers());

            return webServiceMessageSender;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }
}