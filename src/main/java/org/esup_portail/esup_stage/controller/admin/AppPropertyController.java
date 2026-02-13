package org.esup_portail.esup_stage.controller.admin;

import org.esup_portail.esup_stage.controller.ApiController;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.proprety.ConfigMissingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@ApiController
@RequestMapping("/admin/config")
public class AppPropertyController {

    @Autowired
    private ConfigMissingService configMissingService;

    @GetMapping("/missing")
    @Secure
    public Map<String, List<String>> getMissing() {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (utilisateur == null || !UtilisateurHelper.isAdmin(utilisateur)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Accès interdit");
        }
        return Map.of("missing", configMissingService.getMissingKeys());
    }
}
