package org.esup_portail.esup_stage.config.properties.signature;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "docaposte")
public class DocaposteProperties {
    private String uri;
    private String siren;
    private String keystorePath;
    private String keystorePassword;
    private String truststorePath;
    private String truststorePassword;
}

