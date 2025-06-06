package org.esup_portail.esup_stage.config;


import org.esup_portail.esup_stage.config.filters.PublicTokenFilter;
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
@Order(1)
public class PublicSecurityConfiguration {

    public static String PATH_FILTER = "/public";

    @Autowired
    public PublicTokenFilter tokenFilter;

    @Bean
    public SecurityFilterChain filterChainPublic(HttpSecurity http) throws Exception {
        http
                .securityMatcher(PATH_FILTER+"/**")
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PATH_FILTER+"/api-docs/**", PATH_FILTER+"/swagger-ui.html", PATH_FILTER+"/swagger-ui/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
