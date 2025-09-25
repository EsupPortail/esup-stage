package org.esup_portail.esup_stage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Order(2)
public class EvaluationTuteurSecurityConfiguration {

    public static final String EVALUATION_TUTEUR_PATH = "/evaluation-tuteur";
    public static final String EVALUATION_TUTEUR_API_PATH = "/api/evaluation-tuteur";

    @Bean
    public SecurityFilterChain evaluationTuteurSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(EVALUATION_TUTEUR_PATH + "/**", EVALUATION_TUTEUR_API_PATH + "/**")
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }
}