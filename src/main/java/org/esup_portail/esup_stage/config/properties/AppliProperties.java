package org.esup_portail.esup_stage.config.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "appli")
public class AppliProperties {
    private DatasourceProperties datasource;
    private MailerProperties mailer;
    private String prefix;
    private String url;
    private String localapi;
    private String adminTechnique;
    private String dataDir;
    private List<String> publicTokens;

    public Set<String> getAdminTechnique() {
        if (adminTechnique == null || adminTechnique.trim().isEmpty()) {
            return Set.of(); // Retourne un Set vide si `adminTechnique` est null ou vide
        }
        return Arrays.stream(adminTechnique.split(";"))
                .map(String::trim)
                .collect(Collectors.toSet());
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

    @Data
    @NoArgsConstructor
    public static class DatasourceProperties {
        private String url;
        private String username;
        private String password;
        private String driver;
    }

    @Data
    @NoArgsConstructor
    public static class MailerProperties {
        private String protocol;
        private String host;
        private boolean sslEnable;
        private int port;
        private boolean auth;
        private String username;
        private String password;
        private String from;
        private boolean disableDelivery;
        private String deliveryAddress;
    }
}
