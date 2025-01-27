package org.esup_portail.esup_stage.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "referentiel")
public class ReferentielProperties {

    private String wsLogin;
    private String wsPassword;
    private String ldapUrl;
    private String apogeeUrl;

    public String getWsLogin() {
        return wsLogin;
    }

    public void setWsLogin(String wsLogin) {
        this.wsLogin = wsLogin;
    }

    public String getWsPassword() {
        return wsPassword;
    }

    public void setWsPassword(String wsPassword) {
        this.wsPassword = wsPassword;
    }

    public String getLdapUrl() {
        return ldapUrl;
    }

    public void setLdapUrl(String ldapUrl) {
        this.ldapUrl = ldapUrl;
    }

    public String getApogeeUrl() {
        return apogeeUrl;
    }

    public void setApogeeUrl(String apogeeUrl) {
        this.apogeeUrl = apogeeUrl;
    }
}
