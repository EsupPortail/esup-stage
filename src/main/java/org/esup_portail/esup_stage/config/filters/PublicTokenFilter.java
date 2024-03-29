package org.esup_portail.esup_stage.config.filters;

import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Arrays;

public class PublicTokenFilter extends OncePerRequestFilter {

    private final ApplicationBootstrap applicationBootstrap;

    public PublicTokenFilter(ApplicationBootstrap applicationBootstrap) {
        this.applicationBootstrap = applicationBootstrap;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            String token = authorizationHeader.substring(7);
            if (!authorizationHeader.startsWith("Bearer") || Arrays.stream(applicationBootstrap.getAppConfig().getAppPublicTokens()).noneMatch(t -> t.equals(token))) {
                throw new AccessDeniedException("Access denied");
            }

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(token, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }
}
