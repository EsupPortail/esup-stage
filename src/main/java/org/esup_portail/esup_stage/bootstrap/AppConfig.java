package org.esup_portail.esup_stage.bootstrap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AppConfig {
    private String casUrlLogin;
    private String casUrlLogout;
    private String casUrlService;
    private String datasourceUrl;
    private String datasourceUsername;
    private String datasourcePassword;
    private String datasourceDriver;
    private String url;
    private List<String> adminTechs = new ArrayList<>();
    private String referentielWsLogin;
    private String referentielWsPassword;
    private String referentielWsLdapUrl;
    private String referentielWsApogeeUrl;
    private String mailerProtocol;
    private String mailerHost;
    private int mailerPort;
    private boolean mailerAuth;
    private String mailerUsername;
    private String mailerPassword;
    private String mailerFrom;
    private boolean mailerDisableDelivery;
    private String mailerDeliveryAddress;

    public String getCasUrlLogin() {
        return casUrlLogin;
    }

    public void setCasUrlLogin(String casUrlLogin) {
        this.casUrlLogin = casUrlLogin;
    }

    public String getCasUrlLogout() {
        return casUrlLogout;
    }

    public void setCasUrlLogout(String casUrlLogout) {
        this.casUrlLogout = casUrlLogout;
    }

    public String getCasUrlService() {
        return casUrlService;
    }

    public void setCasUrlService(String casUrlService) {
        this.casUrlService = casUrlService;
    }

    public String getDatasourceUrl() {
        return datasourceUrl;
    }

    public void setDatasourceUrl(String datasourceUrl) {
        this.datasourceUrl = datasourceUrl;
    }

    public String getDatasourceUsername() {
        return datasourceUsername;
    }

    public void setDatasourceUsername(String datasourceUsername) {
        this.datasourceUsername = datasourceUsername;
    }

    public String getDatasourcePassword() {
        return datasourcePassword;
    }

    public void setDatasourcePassword(String datasourcePassword) {
        this.datasourcePassword = datasourcePassword;
    }

    public String getDatasourceDriver() {
        return datasourceDriver;
    }

    public void setDatasourceDriver(String datasourceDriver) {
        this.datasourceDriver = datasourceDriver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getAdminTechs() {
        return adminTechs;
    }

    public void setAdminTechs(List<String> adminTechs) {
        this.adminTechs = adminTechs;
    }

    public String getReferentielWsLogin() {
        return referentielWsLogin;
    }

    public void setReferentielWsLogin(String referentielWsLogin) {
        this.referentielWsLogin = referentielWsLogin;
    }

    public String getReferentielWsPassword() {
        return referentielWsPassword;
    }

    public void setReferentielWsPassword(String referentielWsPassword) {
        this.referentielWsPassword = referentielWsPassword;
    }

    public String getReferentielWsLdapUrl() {
        return referentielWsLdapUrl;
    }

    public void setReferentielWsLdapUrl(String referentielWsLdapUrl) {
        this.referentielWsLdapUrl = referentielWsLdapUrl;
    }

    public String getReferentielWsApogeeUrl() {
        return referentielWsApogeeUrl;
    }

    public void setReferentielWsApogeeUrl(String referentielWsApogeeUrl) {
        this.referentielWsApogeeUrl = referentielWsApogeeUrl;
    }

    public String getMailerProtocol() {
        return mailerProtocol;
    }

    public void setMailerProtocol(String mailerProtocol) {
        this.mailerProtocol = mailerProtocol;
    }

    public String getMailerHost() {
        return mailerHost;
    }

    public void setMailerHost(String mailerHost) {
        this.mailerHost = mailerHost;
    }

    public int getMailerPort() {
        return mailerPort;
    }

    public void setMailerPort(int mailerPort) {
        this.mailerPort = mailerPort;
    }

    public boolean getMailerAuth() {
        return mailerAuth;
    }

    public void setMailerAuth(boolean mailerAuth) {
        this.mailerAuth = mailerAuth;
    }

    public String getMailerUsername() {
        return mailerUsername;
    }

    public void setMailerUsername(String mailerUsername) {
        this.mailerUsername = mailerUsername;
    }

    public String getMailerPassword() {
        return mailerPassword;
    }

    public void setMailerPassword(String mailerPassword) {
        this.mailerPassword = mailerPassword;
    }

    public String getMailerFrom() {
        return mailerFrom;
    }

    public void setMailerFrom(String mailerFrom) {
        this.mailerFrom = mailerFrom;
    }

    public boolean getMailerDisableDelivery() {
        return mailerDisableDelivery;
    }

    public void setMailerDisableDelivery(boolean mailerDisableDelivery) {
        this.mailerDisableDelivery = mailerDisableDelivery;
    }

    public String getMailerDeliveryAddress() {
        return mailerDeliveryAddress;
    }

    public void setMailerDeliveryAddress(String mailerDeliveryAddress) {
        this.mailerDeliveryAddress = mailerDeliveryAddress;
    }

    public void initProperties(Properties props, String prefixeProps) {
        this.casUrlLogout = props.getProperty("cas.url.logout");
        this.casUrlLogin = props.getProperty("cas.url.login");
        this.casUrlService = props.getProperty("cas.url.service");

        this.datasourceUrl = props.getProperty(prefixeProps+"datasource.url");
        this.datasourceUsername = props.getProperty(prefixeProps+"datasource.username");
        this.datasourcePassword = props.getProperty(prefixeProps+"datasource.password");
        this.datasourceDriver = props.getProperty(prefixeProps+"datasource.driver");

        this.url = props.getProperty(prefixeProps+"url");
        if (props.containsKey(prefixeProps+"admin_technique")) {
            this.adminTechs = Arrays.asList(props.getProperty(prefixeProps+"admin_technique").split(";"));
        }

        this.referentielWsLogin = props.getProperty("referentiel.ws.login");
        this.referentielWsPassword = props.getProperty("referentiel.ws.password");
        this.referentielWsLdapUrl = props.getProperty("referentiel.ws.ldap_url");
        this.referentielWsApogeeUrl = props.getProperty("referentiel.ws.apogee_url");

        this.mailerProtocol = props.getProperty(prefixeProps+"mailer.protocol");
        this.mailerHost = props.getProperty(prefixeProps+"mailer.host");
        this.mailerPort = Integer.parseInt(props.getProperty(prefixeProps+"mailer.port"));
        this.mailerAuth = Boolean.parseBoolean(props.getProperty(prefixeProps+"mailer.auth"));
        this.mailerUsername = props.getProperty(prefixeProps+"mailer.username");
        this.mailerPassword = props.getProperty(prefixeProps+"mailer.password");
        this.mailerFrom = props.getProperty(prefixeProps+"mailer.from");
        if (props.containsKey(prefixeProps+"mailer.disable_delivery")) {
            this.mailerDisableDelivery = Boolean.parseBoolean(props.getProperty(prefixeProps+"mailer.disable_delivery"));
        } else {
            this.mailerDisableDelivery = false;
        }
        if (props.containsKey(prefixeProps+"mailer.delivery_address") && !props.getProperty(prefixeProps+"mailer.delivery_address").equals("null")) {
            this.mailerDeliveryAddress = props.getProperty(prefixeProps+"mailer.delivery_address");
        }
    }

    @Override
    public String toString() {
        return "AppConfig{" +
                ", casUrlLogin='" + casUrlLogin + "'" +
                ", casUrlLogout='" + casUrlLogout + "'" +
                ", casUrlService='" + casUrlService + "'" +
                ", datasourceUrl='" + datasourceUrl + "'" +
                ", datasourceUsername='" + datasourceUsername + "'" +
                ", datasourceDriver='" + datasourceDriver + "'" +
                ", url='" + url + "'" +
                ", adminTechs='" + adminTechs + "'" +
                ", referentielWsLogin='" + referentielWsLogin + "'" +
                ", referentielWsLdapUrl='" + referentielWsLdapUrl + "'" +
                ", referentielWsApogeeUrl='" + referentielWsApogeeUrl + "'" +
                ", mailerProtocol='" + mailerProtocol + "'" +
                ", mailerHost='" + mailerHost + "'" +
                ", mailerPort='" + mailerPort + "'" +
                ", mailerAuth='" + mailerAuth + "'" +
                ", mailerUsername='" + mailerUsername + "'" +
                ", mailerFrom='" + mailerFrom + "'" +
                ", mailerDisableDelivery='" + mailerDisableDelivery + "'" +
                ", mailerDeliveryAddress='" + mailerDeliveryAddress + "'" +
                "}";
    }
}
