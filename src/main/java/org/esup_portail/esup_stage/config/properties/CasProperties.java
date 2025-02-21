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
        private String login = "/login";
        private String logout = "/logout";

        public String getLoginUrl() {
            String url = service + login;
            if (StringUtils.hasText(login) && login.startsWith("http")) {
                url = login;
            }
            return url;
        }

        public String getLogoutUrl() {
            String url = service + logout;
            if (StringUtils.hasText(logout) && logout.startsWith("http")) {
                url = logout;
            }
            return url;
        }
    }
}
