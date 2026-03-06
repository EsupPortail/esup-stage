package org.esup_portail.esup_stage.config.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.esup_portail.esup_stage.dto.MaintenanceStateDto;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.service.maintenance.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MaintenanceModeFilter extends OncePerRequestFilter {

    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (!uri.startsWith("/api/") || isExcludedUri(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean effectiveMaintenanceActive = maintenanceService.isEffectiveMaintenanceActive();
        if (!effectiveMaintenanceActive || isAdmin()) {
            filterChain.doFilter(request, response);
            return;
        }

        MaintenanceStateDto state = maintenanceService.getCurrentState();

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
        return uri.startsWith("/api/maintenance/status");
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities() != null
                && authentication.getAuthorities().stream().anyMatch(a -> Role.ADM.equals(a.getAuthority()))) {
            return true;
        }

        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        return utilisateur != null && UtilisateurHelper.isAdmin(utilisateur);
    }
}
