package org.esup_portail.esup_stage.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.esup_portail.esup_stage.controller.ApiController;
import org.esup_portail.esup_stage.dto.ActiveSessionDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AdminService;
import org.esup_portail.esup_stage.service.session.ActiveSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@ApiController
@RequestMapping("/admin/sessions")
public class AdminSessionController {

    @Autowired
    private ActiveSessionService activeSessionService;

    @Autowired
    private AdminService adminService;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public List<ActiveSessionDto> list(HttpServletRequest request) {
        adminService.requireAdmin();
        return activeSessionService.listActiveSessions(getCurrentSessionId(request));
    }

    @PostMapping("/{sessionId}/close")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public void close(@PathVariable("sessionId") String sessionId, HttpServletRequest request) {
        adminService.requireAdmin();
        activeSessionService.closeSession(sessionId, getCurrentSessionId(request));
    }

    private String getCurrentSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? session.getId() : null;
    }
}
