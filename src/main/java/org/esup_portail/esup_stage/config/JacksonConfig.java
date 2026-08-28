package org.esup_portail.esup_stage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapper objectMapper() {
        return JsonMapper.builder()
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false) // Ignore les beans vides
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // Ignore les propriétés inconnues
                // Jackson 3 sérialise les dates en ISO-8601 par défaut, là où Jackson 2 utilisait
                // des timestamps numériques. On conserve explicitement le contrat d'origine pour
                // ne pas modifier le format des dates exposé au frontend et à l'API publique.
                .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}
