package org.esup_portail.esup_stage.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.client.session.SingleSignOutFilter;
import org.apereo.cas.client.validation.Cas20ServiceTicketValidator;
import org.apereo.cas.client.validation.TicketValidator;
import org.apereo.cas.client.validation.json.Cas30JsonServiceTicketValidator;
import org.esup_portail.esup_stage.config.filters.CsrfCookieFilter;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.config.properties.CasProperties;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Configuration
@Order(4)
@Slf4j
public class SecurityConfiguration {

    @Autowired
    private AppliProperties appliProperties;

    @Autowired
    private CasProperties casProperties;

    @Bean
    public ServiceProperties serviceProperties() {
        log.info("serviceProperties : {}/login/cas", appliProperties.getUrl());
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(appliProperties.getUrl() + "/login/cas");
        serviceProperties.setSendRenew(false);
        return serviceProperties;
    }

    @Bean
    public AuthenticationEntryPoint casEntryPoint() {
        log.info("casEntryPoint : " + casProperties.getUrl().getLoginUrl());
        CasAuthenticationEntryPoint entryPoint = new CasAuthenticationEntryPoint();
        entryPoint.setLoginUrl(casProperties.getUrl().getLoginUrl());
        entryPoint.setServiceProperties(serviceProperties());
        return entryPoint;
    }

    @Bean
    public TicketValidator ticketValidator() {
        log.info("ticketValidator : {}", casProperties.getUrl().getService());
        if (casProperties.getResponseType() != null && casProperties.getResponseType().equals("xml")) {
            return new Cas20ServiceTicketValidator(casProperties.getUrl().getService());
        } else {
            return new Cas30JsonServiceTicketValidator(casProperties.getUrl().getService());
        }
    }

    @Bean
    public AuthenticationUserDetailsService<CasAssertionAuthenticationToken> casUserDetailsService() {
        return new CasUserDetailsServiceImpl();
    }

    @Bean
    public CasAuthenticationProvider casAuthenticationProvider() {
        CasAuthenticationProvider provider = new CasAuthenticationProvider();
        provider.setServiceProperties(serviceProperties());
        provider.setTicketValidator(ticketValidator());
        provider.setAuthenticationUserDetailsService(casUserDetailsService());
        provider.setKey("CAS_PROVIDER");
        return provider;
    }

    @Bean
    public CasAuthenticationFilter casAuthenticationFilter() {
        CasAuthenticationFilter filter = new CasAuthenticationFilter();
        filter.setServiceProperties(serviceProperties());
        filter.setAuthenticationManager(new ProviderManager(Collections.singletonList(casAuthenticationProvider())));
        filter.setAuthenticationFailureHandler((req, res, ex) -> casEntryPoint().commence(req, res, ex));
        filter.setFilterProcessesUrl("/login/cas"); // explicite
        return filter;
    }

    @Bean
    public SingleSignOutFilter singleSignOutFilter() {
        SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
        singleSignOutFilter.setIgnoreInitConfiguration(true);
        return singleSignOutFilter;
    }

    @Bean
    public LogoutFilter logoutFilter() {
        log.info("logoutFilter : {}", casProperties.getUrl().getLogoutUrl());
        LogoutFilter logoutFilter = new LogoutFilter(casProperties.getUrl().getLogoutUrl(), new SecurityContextLogoutHandler());
        logoutFilter.setFilterProcessesUrl("/logout");
        return logoutFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        // Routes d'authentification
                        .requestMatchers("/login/cas").permitAll()
                        .requestMatchers("/api/version").permitAll()
                        .requestMatchers("/api/contenus/**").permitAll()
                        .requestMatchers("/api/config/theme").permitAll()
                        .requestMatchers("/api/evaluation-tuteur/**").permitAll()
                        .requestMatchers("/error/**").permitAll()
                        .requestMatchers("/frontend/**").permitAll()
                        .requestMatchers("/theme.css").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler())
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(401);
                            } else {
                                response.sendRedirect("/login/cas");
                            }
                        })
                )
                .addFilter(casAuthenticationFilter())
                .addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class)
                .addFilterBefore(logoutFilter(), LogoutFilter.class)
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .csrf(csrf -> csrf
                        // Utilise un cookie accessible en JS (non HttpOnly)
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // Spring 6+ : nécessaire pour forcer la génération du cookie
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                );

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.sendRedirect("/error-401");
        };
    }

    /** Configuration CORS pour autoriser les requêtes depuis le frontend Angular en DEV */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // indispensable pour que les cookies passent
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
