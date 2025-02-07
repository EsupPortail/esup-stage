package org.esup_portail.esup_stage.config.properties.signature;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "webhook.signature")
public class WebhookProperties {
    private String uri;
    private String token;
}
