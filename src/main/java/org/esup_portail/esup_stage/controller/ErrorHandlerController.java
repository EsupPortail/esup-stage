package org.esup_portail.esup_stage.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.esup_portail.esup_stage.dto.ConfigThemeDto;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorHandlerController implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandlerController.class);

    @Autowired
    private AppConfigService appConfigService;

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, ModelMap model) {
        try {
            ConfigThemeDto configTheme = appConfigService.getConfigTheme();
            if (configTheme != null) {
                if (configTheme.getFavicon() != null) {
                    model.addAttribute("favicon", "data:" + configTheme.getFavicon().getContentType() + ";base64," + configTheme.getFavicon().getBase64());
                }
                if (configTheme.getLogo() != null) {
                    model.addAttribute("logo", "data:" + configTheme.getLogo().getContentType() + ";base64," + configTheme.getLogo().getBase64());
                }
            }
        } catch (RuntimeException e) {
            log.warn("Unable to load theme for error page: {}", e.getMessage());
        }

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                return "error/401";
            }
        }
        return "error";
    }
}
