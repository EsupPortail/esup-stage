package org.esup_portail.esup_stage.config.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "referentiel.ws")
public class ReferentielProperties {
    private String login;
    private String password;
    private String ldapUrl;
    private String apogeeUrl;
}
