package org.esup_portail.esup_stage.config.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "sirene")
public class SireneProperties {
    private String url;
    private String token;
    private Integer nombreMinimumResultats;

    public boolean isApiSireneActive() {
        return StringUtils.hasText(url)
                && StringUtils.hasText(token)
                && nombreMinimumResultats != null
                && nombreMinimumResultats > 0;
    }
}