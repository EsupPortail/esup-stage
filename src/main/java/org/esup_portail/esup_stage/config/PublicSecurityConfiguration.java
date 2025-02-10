package org.esup_portail.esup_stage.config;


import org.esup_portail.esup_stage.config.filters.PublicTokenFilter;
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

    @Bean
    public SecurityFilterChain filterChainPublic(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/public/**")
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/public/api-docs/**", "/public/swagger-ui.html", "/public/swagger-ui/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }


    public PublicTokenFilter tokenFilter() {
        return new PublicTokenFilter();
    }
}
