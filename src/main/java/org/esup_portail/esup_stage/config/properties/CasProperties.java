package org.esup_portail.esup_stage.config.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "cas")
public class CasProperties {
    private UrlProperties url = new UrlProperties();
    private String responseType;

    @Data
    @NoArgsConstructor
    public static class UrlProperties {
        private String service;
        private String login="/login";
        private String logout="/logout";

        public String getLoginUrl() {
            if(StringUtils.hasText(login) && !login.startsWith("http")) {
                return service + login;
            }
            return service;
        }

        public String getLogoutUrl() {
            if(StringUtils.hasText(logout) && !logout.startsWith("http")) {
                return service + logout;
            }
            return service;
        }
    }
}
