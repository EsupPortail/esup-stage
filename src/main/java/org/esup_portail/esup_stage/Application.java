package org.esup_portail.esup_stage;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.config.properties.signature.WebhookProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Point d'entrée de l'application Spring Boot.
 *
 * @author Matthieu Manginot
 */
@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan(basePackageClasses = {AppliProperties.class, WebhookProperties.class})
public class Application extends SpringBootServletInitializer {
    /**
     * Configure le lancement de l'application via un serveur web embarqué.
     *
     * @param args paramètres
     */
    public static void main(final String[] args) {
        build(new SpringApplicationBuilder(Application.class)).run(args);
    }

    private static SpringApplicationBuilder build(final SpringApplicationBuilder builder) {
        return builder
                /* Charge les propriétés par défaut à partir de application-defaults.yml */
                .profiles("defaults");
    }

    /**
     * Configure le déploiement dans un container web. (WAR)
     */
    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
        return build(builder).sources(Application.class);
    }

}
