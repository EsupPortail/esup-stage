package org.esup_portail.esup_stage.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
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

    private static final Logger logger = LogManager.getLogger(DocaposteConfig.class);

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("org.esup_portail.esup_stage.docaposte.gen");
        return marshaller;
    }

    @Bean
    public DocaposteClient docaposteClient(Jaxb2Marshaller marshaller) {
        DocaposteClient client = new DocaposteClient();
        if (applicationBootstrap.getAppConfig().isDocaposteEnabled()) {
            client.setDefaultUri(applicationBootstrap.getAppConfig().getDocaposteUri());
            client.setMarshaller(marshaller);
            client.setUnmarshaller(marshaller);
            client.getWebServiceTemplate().setMessageSender(createWebServiceMessageSender());
        }
        return client;
    }

    private WebServiceMessageSender createWebServiceMessageSender() {
        if (applicationBootstrap.getAppConfig().isDocaposteEnabled()) {
            String docaposteKeystorePath = applicationBootstrap.getAppConfig().getDocaposteKeystorePath();
            String docaposteKeystorePassword = applicationBootstrap.getAppConfig().getDocaposteKeystorePassword();
            String docaposteTruststorePath = applicationBootstrap.getAppConfig().getDocaposteTruststorePath();
            String docaposteTruststorePassword = applicationBootstrap.getAppConfig().getDocaposteTruststorePassword();

            try {
                KeyStore ks = KeyStore.getInstance("PKCS12");
                logger.info("Loaded keystore: " + docaposteKeystorePath);
                try (InputStream is = new FileInputStream(docaposteKeystorePath)) {
                    ks.load(is, docaposteKeystorePassword.toCharArray());
                }
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(ks, docaposteKeystorePassword.toCharArray());

                KeyStore ts = KeyStore.getInstance("JKS");
                logger.info("Loaded truststore: " + docaposteTruststorePath);
                try (InputStream is = new FileInputStream(docaposteTruststorePath)) {
                    ts.load(is, docaposteTruststorePassword.toCharArray());
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
        return null;
    }
}
