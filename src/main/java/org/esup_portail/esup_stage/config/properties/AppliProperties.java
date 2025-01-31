package org.esup_portail.esup_stage.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "appli")
public class AppliProperties {

    private  DatasourceProperties datasource;
    private  MailerProperties mailer;
    private  String prefix;
    private  String url;
    private  String localapi;
    private  String adminTechnique;
    private  String dataDir;
    private  List<String> publicTokens;

    public  DatasourceProperties getDatasource() {
        return datasource;
    }

    public void setDatasource(DatasourceProperties datasource) {
        this.datasource = datasource;
    }

    public  MailerProperties getMailer() {
        return mailer;
    }

    public void setMailer(MailerProperties mailer) {
        this.mailer = mailer;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalapi() {
        return localapi;
    }

    public void setLocalapi(String localapi) {
        this.localapi = localapi;
    }

    public Set<String> getAdminTechnique() {
        if (adminTechnique == null || adminTechnique.trim().isEmpty()) {
            return Set.of(); // Retourne un Set vide si `adminTechnique` est null ou vide
        }
        return Arrays.stream(adminTechnique.split(";"))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    public void setAdminTechnique(String adminTechnique) {
        this.adminTechnique = adminTechnique;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String[] getPublicTokens() {
        if (publicTokens == null || publicTokens.isEmpty()) {
            return new String[]{};
        }

        return publicTokens.stream()
                .filter(StringUtils::hasText) // Filtre les tokens non vides
                .flatMap(token -> Stream.of(token.split(";"))) // Divise les tokens par ";"
                .toArray(String[]::new); // Retourne un tableau
    }

    public void setPublicTokens(List<String> publicTokens) {
        this.publicTokens = publicTokens;
    }


    public static class DatasourceProperties {
        private  String url;
        private  String username;
        private  String password;
        private  String driver;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriver() {
            return driver;
        }

        public void setDriver(String driver) {
            this.driver = driver;
        }
    }

    public static class MailerProperties {
        private  String protocol;
        private  String host;
        private  boolean sslEnable;
        private  int port;
        private  boolean auth;
        private  String username;
        private  String password;
        private  String from;
        private  boolean disableDelivery;
        private  String deliveryAddress;

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public boolean isSslEnable() {
            return sslEnable;
        }

        public void setSslEnable(boolean sslEnable) {
            this.sslEnable = sslEnable;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isAuth() {
            return auth;
        }

        public void setAuth(boolean auth) {
            this.auth = auth;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public boolean isDisableDelivery() {
            return disableDelivery;
        }

        public void setDisableDelivery(boolean disableDelivery) {
            this.disableDelivery = disableDelivery;
        }

        public String getDeliveryAddress() {
            return deliveryAddress;
        }

        public void setDeliveryAddress(String deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
        }
    }
}
