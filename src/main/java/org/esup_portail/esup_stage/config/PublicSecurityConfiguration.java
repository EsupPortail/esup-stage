package org.esup_portail.esup_stage.config;

import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.config.filters.PublicTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@Order(1)
public class PublicSecurityConfiguration {

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @Bean
    public SecurityFilterChain filterChainPublic(HttpSecurity http) throws Exception {
        http.antMatcher("/public/**").authorizeRequests()
                .antMatchers("/public/api-docs/**", "/public/swagger-ui.html", "/public/swagger-ui/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(tokenFilter(), UsernamePasswordAuthenticationFilter.class);

        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }

    public PublicTokenFilter tokenFilter() {
        return new PublicTokenFilter(applicationBootstrap);
    }
}
