package org.esup_portail.esup_stage.config;

import org.apereo.cas.client.session.SingleSignOutFilter;
import org.apereo.cas.client.validation.TicketValidator;
import org.apereo.cas.client.validation.json.Cas30JsonServiceTicketValidator;
import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
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
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.Collections;

@Configuration
@Order(3)
public class SecurityConfiguration {

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @Bean
    public ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(applicationBootstrap.getAppConfig().getPrefix() + "/login/cas");
        serviceProperties.setSendRenew(false);
        return serviceProperties;
    }

    @Bean
    public AuthenticationEntryPoint casEntryPoint() {
        CasAuthenticationEntryPoint entryPoint = new CasAuthenticationEntryPoint();
        entryPoint.setLoginUrl(applicationBootstrap.getAppConfig().getCasUrlLogin());
        entryPoint.setServiceProperties(serviceProperties());
        return entryPoint;
    }

    @Bean
    public TicketValidator ticketValidator() {
        return new Cas30JsonServiceTicketValidator(applicationBootstrap.getAppConfig().getCasUrlService());
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
        LogoutFilter logoutFilter = new LogoutFilter(applicationBootstrap.getAppConfig().getCasUrlLogout(), new SecurityContextLogoutHandler());
        logoutFilter.setFilterProcessesUrl("/logout");
        return logoutFilter;
    }

    @Bean
    public SecurityFilterChain filterChainPrivate(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login/cas", "/api/version").permitAll()  // Autoriser ces URLs sans authentification
                        .anyRequest().authenticated()  // Toutes les autres requêtes doivent être authentifiées
                )
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(casEntryPoint())  // Gestion des exceptions d'authentification
                )
                .addFilter(casAuthenticationFilter())  // Ajouter le filtre d'authentification CAS
                .addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class)  // Ajouter le filtre de déconnexion avant le filtre CAS
                .addFilterBefore(logoutFilter(), LogoutFilter.class)  // Ajouter le filtre de déconnexion

                .csrf(AbstractHttpConfigurer::disable);  // Désactiver CSRF

        return http.build();
    }

}
