package org.esup_portail.esup_stage.config;

import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.config.filters.WebhookEsupSignatureTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Order(2)
public class EsupSignatureConfiguration {

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @Bean
    public SecurityFilterChain filterChainWebhookEsupSignature(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/webhook/**")
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));  // Sessions stateless

        return http.build();
    }


    public WebhookEsupSignatureTokenFilter tokenFilter() {
        return new WebhookEsupSignatureTokenFilter(applicationBootstrap);
    }
}
