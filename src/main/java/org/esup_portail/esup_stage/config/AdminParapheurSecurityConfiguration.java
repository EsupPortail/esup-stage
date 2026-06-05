package org.esup_portail.esup_stage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import java.net.InetAddress;

@Configuration
@Order(2)
public class AdminParapheurSecurityConfiguration {

    public static final String PATH_FILTER = "/adminparapheur/remote-service";

    @Bean
    public SecurityFilterChain filterChainAdminParapheur(HttpSecurity http) throws Exception {
        http
                .securityMatcher(PATH_FILTER + "/**")
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().access((authentication, context) ->
                                new AuthorizationDecision(isLoopbackAddress(context.getRequest().getRemoteAddr())))
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> response.sendError(403))
                        .accessDeniedHandler((request, response, accessDeniedException) -> response.sendError(403))
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    private static boolean isLoopbackAddress(String remoteAddress) {
        try {
            return InetAddress.getByName(remoteAddress).isLoopbackAddress();
        } catch (Exception e) {
            return false;
        }
    }
}
