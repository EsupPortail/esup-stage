package org.esup_portail.esup_stage.config.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.esup_portail.esup_stage.config.PublicSecurityConfiguration;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Arrays;

@Component
public class PublicTokenFilter extends OncePerRequestFilter {

    private final AppliProperties appliProperties;

    @Autowired
    public PublicTokenFilter(AppliProperties appliProperties) {
        this.appliProperties = appliProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            String token = authorizationHeader;
            if (authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
            }
            if (Arrays.stream(appliProperties.getPublicTokens()).noneMatch(token::equals)) {
                throw new AccessDeniedException("Access denied");
            }

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(token, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Éviter d'exécuter le filtre si l'URL n'est pas "/public"
        return !request.getRequestURI().startsWith(PublicSecurityConfiguration.PATH_FILTER);
    }
}
