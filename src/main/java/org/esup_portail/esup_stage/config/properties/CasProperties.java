package org.esup_portail.esup_stage.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "cas")
public class CasProperties {

    private UrlProperties url = new UrlProperties();

    private String responseType;

    public UrlProperties getUrl() {
        return url;
    }

    public void setUrl(UrlProperties url) {
        this.url = url;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    // Sous-classe pour les propriétés d'URL
    public static class UrlProperties {
        private String login;
        private String service;
        private String logout;

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getLogout() {
            return logout;
        }

        public void setLogout(String logout) {
            this.logout = logout;
        }
    }
}
