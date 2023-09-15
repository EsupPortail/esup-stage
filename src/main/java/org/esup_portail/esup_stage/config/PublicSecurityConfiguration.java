package org.esup_portail.esup_stage.config;

import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
@Order(1)
public class PublicSecurityConfiguration {

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .inMemoryAuthentication()
                .withUser(applicationBootstrap.getAppConfig().getAppPublicLogin())
                .password(passwordEncoder().encode(applicationBootstrap.getAppConfig().getAppPublicPassword()))
                .authorities("ROLE_USER");
    }

    @Bean
    public SecurityFilterChain filterChainPublic(HttpSecurity http) throws Exception {
        http.antMatcher("/public/**")
                .authorizeRequests().anyRequest().authenticated()
                .and()
                .httpBasic();

        http.csrf().disable();

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
