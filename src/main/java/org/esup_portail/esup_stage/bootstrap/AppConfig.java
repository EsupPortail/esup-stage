package org.esup_portail.esup_stage.bootstrap;

import org.apache.logging.log4j.util.Strings;
import org.esup_portail.esup_stage.enums.AppSignatureEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AppConfig {
    private String casResponseType;
    private String casUrlLogin;
    private String casUrlLogout;
    private String casUrlService;
    private String datasourceUrl;
    private String datasourceUsername;
    private String datasourcePassword;
    private String datasourceDriver;
    private String prefix;
    private String localApi;
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
    private boolean mailerSsl;
    private String mailerUsername;
    private String mailerPassword;
    private String mailerFrom;
    private boolean mailerDisableDelivery;
    private String mailerDeliveryAddress;
    private String dataDir;
    private String docaposteUri;
    private String docaposteSiren;
    private String docaposteKeystorePath;
    private String docaposteKeystorePassword;
    private String docaposteTruststorePath;
    private String docaposteTruststorePassword;
    private boolean docaposteEnabled = false;
    private String[] appPublicTokens;
    private String webhookSignatureUri;
    private String webhookSignatureToken;
    private String esupSignatureUri;
    private AppSignatureEnum appSignatureEnabled;

    public String getCasResponseType() {
        return casResponseType;
    }

    public void setCasResponseType(String casResponseType) {
        this.casResponseType = casResponseType;
    }

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

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getLocalApi() {
        return localApi;
    }

    public void setLocalApi(String localApi) {
        this.localApi = localApi;
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

    public boolean isMailerSsl() {
        return mailerSsl;
    }

    public void setMailerSsl(boolean mailerSsl) {
        this.mailerSsl = mailerSsl;
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

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getDocaposteUri() {
        return docaposteUri;
    }

    public void setDocaposteUri(String docaposteUri) {
        this.docaposteUri = docaposteUri;
    }

    public String getDocaposteSiren() {
        return docaposteSiren;
    }

    public void setDocaposteSiren(String docaposteSiren) {
        this.docaposteSiren = docaposteSiren;
    }

    public String getDocaposteKeystorePath() {
        return docaposteKeystorePath;
    }

    public void setDocaposteKeystorePath(String docaposteKeystorePath) {
        this.docaposteKeystorePath = docaposteKeystorePath;
    }

    public String getDocaposteKeystorePassword() {
        return docaposteKeystorePassword;
    }

    public void setDocaposteKeystorePassword(String docaposteKeystorePassword) {
        this.docaposteKeystorePassword = docaposteKeystorePassword;
    }

    public String getDocaposteTruststorePath() {
        return docaposteTruststorePath;
    }

    public void setDocaposteTruststorePath(String docaposteTruststorePath) {
        this.docaposteTruststorePath = docaposteTruststorePath;
    }

    public String getDocaposteTruststorePassword() {
        return docaposteTruststorePassword;
    }

    public void setDocaposteTruststorePassword(String docaposteTruststorePassword) {
        this.docaposteTruststorePassword = docaposteTruststorePassword;
    }

    public boolean isDocaposteEnabled() {
        return docaposteEnabled;
    }

    public void setDocaposteEnabled(boolean docaposteEnabled) {
        this.docaposteEnabled = docaposteEnabled;
    }

    public String getWebhookSignatureUri() {
        return webhookSignatureUri;
    }

    public void setWebhookSignatureUri(String webhookSignatureUri) {
        this.webhookSignatureUri = webhookSignatureUri;
    }

    public String[] getAppPublicTokens() {
        return appPublicTokens;
    }

    public void setAppPublicTokens(String[] appPublicTokens) {
        this.appPublicTokens = appPublicTokens;
    }

    public String getWebhookSignatureToken() {
        return webhookSignatureToken;
    }

    public void setWebhookSignatureToken(String webhookSignatureToken) {
        this.webhookSignatureToken = webhookSignatureToken;
    }

    public String getEsupSignatureUri() {
        return esupSignatureUri;
    }

    public void setEsupSignatureUri(String esupSignatureUri) {
        this.esupSignatureUri = esupSignatureUri;
    }

    public AppSignatureEnum getAppSignatureEnabled() {
        return appSignatureEnabled;
    }

    public void setAppSignatureEnabled(AppSignatureEnum appSignatureEnabled) {
        this.appSignatureEnabled = appSignatureEnabled;
    }

    public void initProperties(Properties props, String prefixeProps) {
        if (props.containsKey("cas.response_type") && !Strings.isEmpty(props.getProperty("cas.response_type"))) {
            this.casResponseType = props.getProperty("cas.response_type");
        } else {
            this.casResponseType = "json";
        }
        this.casUrlLogout = props.getProperty("cas.url.logout");
        this.casUrlLogin = props.getProperty("cas.url.login");
        this.casUrlService = props.getProperty("cas.url.service");

        this.datasourceUrl = props.getProperty(prefixeProps+"datasource.url");
        this.datasourceUsername = props.getProperty(prefixeProps+"datasource.username");
        this.datasourcePassword = props.getProperty(prefixeProps+"datasource.password");
        this.datasourceDriver = props.getProperty(prefixeProps+"datasource.driver");

        this.prefix = props.getProperty(prefixeProps+"prefix");
        this.url = props.getProperty(prefixeProps+"url");
        this.localApi = props.getProperty(prefixeProps+"localapi");
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
        if (props.containsKey(prefixeProps+"mailer.ssl.enable")) {
            this.mailerSsl = Boolean.parseBoolean(props.getProperty(prefixeProps+"mailer.ssl.enable"));
        } else {
            this.mailerSsl = false;
        }
        this.dataDir = props.getProperty(prefixeProps+"data_dir");

        this.docaposteUri = props.getProperty("docaposte.uri");
        this.docaposteSiren = props.getProperty("docaposte.siren");
        this.docaposteKeystorePath = props.getProperty("docaposte.keystore.path");
        this.docaposteKeystorePassword = props.getProperty("docaposte.keystore.password");
        this.docaposteTruststorePath = props.getProperty("docaposte.truststore.path");
        this.docaposteTruststorePassword = props.getProperty("docaposte.truststore.password");
        this.docaposteEnabled = this.docaposteUri != null && this.docaposteSiren != null && this.docaposteKeystorePath != null && this.docaposteKeystorePassword != null && this.docaposteTruststorePath != null && this.docaposteTruststorePassword != null;

        this.appPublicTokens = new String[]{};
        if (props.containsKey(prefixeProps+"public.tokens") && !Strings.isEmpty(props.getProperty(prefixeProps+"public.tokens"))) {
            String tokens = props.getProperty(prefixeProps+"public.tokens");
            this.appPublicTokens = tokens.split(";");
        }

        if (props.containsKey("webhook.signature.uri") && !Strings.isEmpty(props.getProperty("webhook.signature.uri"))) {
            this.webhookSignatureUri = props.getProperty("webhook.signature.uri");
        }
        if (props.containsKey("webhook.signature.token") && !Strings.isEmpty(props.getProperty("webhook.signature.token"))) {
            this.webhookSignatureToken = props.getProperty("webhook.signature.token");
        }
        if (props.containsKey("esupsignature.uri") && !Strings.isEmpty(props.getProperty("esupsignature.uri"))) {
            this.esupSignatureUri = props.getProperty("esupsignature.uri");
        }
        if (this.docaposteEnabled) {
            this.appSignatureEnabled = AppSignatureEnum.DOCAPOSTE;
        } else {
            if (this.webhookSignatureUri != null && this.webhookSignatureToken != null) {
                this.appSignatureEnabled = AppSignatureEnum.EXTERNE;
                if (this.esupSignatureUri != null) {
                    this.appSignatureEnabled = AppSignatureEnum.ESUPSIGNATURE;
                }
            }
        }

    }

    @Override
    public String toString() {
        return "AppConfig{" +
                ", casResponseType='" + casResponseType + "'" +
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
                ", mailerSsl='" + mailerSsl + "'" +
                ", mailerUsername='" + mailerUsername + "'" +
                ", mailerFrom='" + mailerFrom + "'" +
                ", mailerDisableDelivery='" + mailerDisableDelivery + "'" +
                ", mailerDeliveryAddress='" + mailerDeliveryAddress + "'" +
                ", dataDir='" + dataDir + "'" +
                ", docaposteUri='" + docaposteUri + "'" +
                ", docaposteSiren='" + docaposteSiren + "'" +
                ", docaposteKeystorePath='" + docaposteKeystorePath + "'" +
                ", docaposteTruststorePath='" + docaposteTruststorePath + "'" +
                ", docaposteEnabled='" + docaposteEnabled + "'" +
                ", webhookSignatureUri='" + webhookSignatureUri + "'" +
                ", esupSignatureUri='" + esupSignatureUri + "'" +
                ", appSignatureEnabled='" + appSignatureEnabled + "'" +
                "}";
    }
}
