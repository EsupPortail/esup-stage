package org.esup_portail.esup_stage.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "referentiel.ws")
public class ReferentielProperties {

    private String login;
    private String password;
    private String ldapUrl;
    private String apogeeUrl;

    public String getWsLogin() {
        return login;
    }

    public void setWsLogin(String wsLogin) {
        this.login = wsLogin;
    }

    public String getWsPassword() {
        return password;
    }

    public void setWsPassword(String wsPassword) {
        this.password = wsPassword;
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
