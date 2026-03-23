package org.esup_portail.esup_stage.config.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.MaintenanceStateDto;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.service.maintenance.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MaintenanceModeFilter extends OncePerRequestFilter {

    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppliProperties appliProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (!uri.startsWith("/api/") || isExcludedUri(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        MaintenanceStateDto state = maintenanceService.getLastKnownState();
        if (!state.isActive() || isAdmin()) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        body.put("message", "Application en maintenance");
        body.put("maintenance", state);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private boolean isExcludedUri(String uri) {
        return uri.startsWith("/api/maintenance/status")
                || uri.startsWith("/api/maintenance/stream")
                || uri.startsWith("/api/users/connected")
                || uri.startsWith("/api/users/admintech")
                || uri.startsWith("/api/version")
                || uri.startsWith("/api/contenus/")
                || uri.startsWith("/api/config/theme")
                || uri.startsWith("/api/evaluation-tuteur/");
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities() != null
                && authentication.getAuthorities().stream().anyMatch(a -> Role.ADM.equals(a.getAuthority()))) {
            return true;
        }

        String login = resolveLogin(authentication);
        if (login != null && appliProperties.isAdminTechnique(login)) {
            return true;
        }

        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (utilisateur == null) {
            return false;
        }

        return UtilisateurHelper.isAdmin(utilisateur) || appliProperties.isAdminTechnique(utilisateur.getLogin());
    }

    private String resolveLogin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String authName = authentication.getName();
        if (authName != null && !authName.isBlank()) {
            return authName;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String principalAsString && !principalAsString.isBlank()) {
            return principalAsString;
        }
        return null;
    }
}
