package fr.esupportail.esupstage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import fr.esupportail.esupstage.property.ApplicationProperties;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableConfigurationProperties(value = ApplicationProperties.class)
public class Application extends SpringBootServletInitializer {
	public static void main(final String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
