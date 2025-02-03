package org.esup_portail.esup_stage;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.config.properties.signature.WebhookProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan(basePackageClasses = {AppliProperties.class, WebhookProperties.class})
public class EsupStageApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(EsupStageApplication.class, args);
	}

}
