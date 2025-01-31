package org.esup_portail.esup_stage.config;

import groovy.util.logging.Slf4j;
import org.esup_portail.esup_stage.config.properties.SignatureProperties;
import org.esup_portail.esup_stage.docaposte.DocaposteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;


@Configuration
@Slf4j
@ConditionalOnProperty(prefix="docaposte",name = "uri")
public class DocaposteConfig {

    @Autowired
    private SignatureProperties signatureProperties;

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("org.esup_portail.esup_stage.docaposte.gen");
        return marshaller;
    }

    @Bean
    public DocaposteClient docaposteClient(Jaxb2Marshaller marshaller) {
        DocaposteClient client = new DocaposteClient(signatureProperties);
        client.setDefaultUri(signatureProperties.getDocaposte().getUri());
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        return client;
    }
}