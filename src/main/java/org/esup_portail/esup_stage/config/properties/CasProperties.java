package org.esup_portail.esup_stage.config.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "cas")
public class CasProperties {
    private UrlProperties url = new UrlProperties();
    private String responseType;

    @Data
    @NoArgsConstructor
    public static class UrlProperties {
        private String login;
        private String service;
        private String logout;
    }
}
