package org.esup_portail.esup_stage.config.properties.signature;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "docaposte")
public class DocaposteProperties {
    private String uri;
    private String siren;
    private keystoreProperties keystore;
    private truststoreProperties truststore;

    @Data
    @NoArgsConstructor
    public static class keystoreProperties {
        private String path;
        private String password;
    }

    @Data
    @NoArgsConstructor
    public static class truststoreProperties {
        private String path;
        private String password;
    }


}

