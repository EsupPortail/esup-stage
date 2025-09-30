package org.esup_portail.esup_stage.config.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "appli")
@Component
public class AppliProperties {
    private DatasourceProperties datasource;
    private MailerProperties mailer;
    private String prefix;
    private String url;
    private String localApi;
    private String adminTechnique;
    private String dataDir;
    private List<String> tokens;
    private String jwtSecret;
    private Long nbJoursValideToken;

    public String getLocalApi() {
        String result = prefix;
        if (StringUtils.hasText(localApi)) {
            result = localApi;
        }
        return result;
    }

    public Set<String> getAdminTechnique() {
        if (adminTechnique == null || adminTechnique.trim().isEmpty()) {
            return Set.of(); // Retourne un Set vide si `adminTechnique` est null ou vide
        }
        return Arrays.stream(adminTechnique.split(";"))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    public boolean isAdminTechnique(final String login){
        return getAdminTechnique().contains(login);
    }

    public String[] getPublicTokens() {
        if (tokens == null || tokens.isEmpty()) {
            return new String[]{};
        }
        return tokens.stream()
                .filter(StringUtils::hasText) // Filtre les tokens non vides
                .flatMap(token -> Stream.of(token.split(";"))) // Divise les tokens par ";"
                .toArray(String[]::new);
    }

    @Data
    @NoArgsConstructor
    public static class DatasourceProperties {
        private String url;
        private String username;
        private String password;
        private String driver;
        private String context;
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
