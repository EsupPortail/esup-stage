package org.esup_portail.esup_stage.config.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.esup_portail.esup_stage.config.EsupSignatureConfiguration;
import org.esup_portail.esup_stage.config.properties.SignatureProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;

@Component
public class WebhookEsupSignatureTokenFilter extends OncePerRequestFilter {

    @Autowired
    private SignatureProperties signatureProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            String token = authorizationHeader;
            if (authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
            }
            if (!token.equals(signatureProperties.getWebhook().getToken())) {
                throw new AccessDeniedException("Access denied");
            }

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(token, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Éviter d'exécuter le filtre si l'URL n'est pas "/webhook"
        return !request.getRequestURI().startsWith(EsupSignatureConfiguration.PATH_FILTER);
    }
}
