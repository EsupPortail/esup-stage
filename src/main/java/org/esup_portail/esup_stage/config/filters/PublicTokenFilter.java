package org.esup_portail.esup_stage.config.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.esup_portail.esup_stage.config.PublicSecurityConfiguration;
import org.esup_portail.esup_stage.model.ApiToken;
import org.esup_portail.esup_stage.service.apitoken.ApiTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;

@Component
public class PublicTokenFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublicTokenFilter.class);

    private final ApiTokenService apiTokenService;

    public PublicTokenFilter(ApiTokenService apiTokenService) {
        this.apiTokenService = apiTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        String token = authorizationHeader == null ? null : authorizationHeader.trim();
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        Optional<ApiToken> apiToken = apiTokenService.authenticate(token);
        if (apiToken.isEmpty()) {
            // Token absent, inconnu, désactivé ou supprimé : réponse identique dans tous les cas
            LOGGER.warn("Accès refusé à l'API publique : token invalide ({} {})", request.getMethod(), request.getServletPath());
            writeUnauthorized(response);
            return;
        }

        ApiToken utilise = apiToken.get();
        LOGGER.info("Appel de l'API publique par l'application \"{}\" (token \"{}\") : {} {}", utilise.getNomApplication(), utilise.getNom(), request.getMethod(), request.getServletPath());

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(utilise.getNomApplication(), null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"" + ApiTokenService.MESSAGE_TOKEN_INVALIDE + "\"}");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Éviter d'exécuter le filtre si l'URL n'est pas "/public"
        if (!path.startsWith(PublicSecurityConfiguration.PATH_FILTER)) {
            return true;
        }
        // La documentation de l'API est en accès libre (permitAll dans PublicSecurityConfiguration)
        return PublicSecurityConfiguration.isDocumentationPath(path);
    }
}
