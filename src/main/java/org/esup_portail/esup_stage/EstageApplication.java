package org.esup_portail.esup_stage;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.config.properties.ReferentielProperties;
import org.esup_portail.esup_stage.config.properties.SignatureProperties;
import org.esup_portail.esup_stage.config.properties.signature.DocaposteProperties;
import org.esup_portail.esup_stage.config.properties.signature.EsupSignatureProperties;
import org.esup_portail.esup_stage.config.properties.signature.WebhookProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

//@EnableConfigurationProperties({AppliProperties.class, ReferentielProperties.class, DocaposteProperties.class, EsupSignatureProperties.class, WebhookProperties.class})
@SpringBootApplication
@EnableScheduling
public class EstageApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(EstageApplication.class, args);
	}

}
