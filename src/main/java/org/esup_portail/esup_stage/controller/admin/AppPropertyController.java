package org.esup_portail.esup_stage.controller.admin;

import org.esup_portail.esup_stage.controller.ApiController;
import org.esup_portail.esup_stage.dto.AppPropertyDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.AppProperty;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.proprety.AppProperyService;
import org.esup_portail.esup_stage.service.proprety.ConfigMissingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApiController
@RequestMapping("/admin/config")
public class AppPropertyController {

    @Autowired
    private ConfigMissingService configMissingService;

    @Autowired
    private AppProperyService appProperyService;

    @GetMapping("/missing")
    @Secure
    public Map<String, List<String>> getMissing() {
        requireAdmin();
        return Map.of("missing", configMissingService.getMissingKeys());
    }

    @GetMapping("/required-keys")
    @Secure
    public Map<String, List<String>> getRequiredKeys() {
        requireAdmin();
        return Map.of("required", configMissingService.getRequiredKeys());
    }

    @GetMapping("/properties")
    @Secure
    public List<AppPropertyDto> getAllProperties() {
        requireAdmin();
        List<AppProperty> properties = appProperyService.getAll();
        return properties.stream().map(this::toDto).collect(Collectors.toList());
    }

    @PostMapping("/properties")
    @Secure
    public List<AppPropertyDto> saveProperties(@RequestBody List<AppPropertyDto> properties) {
        Utilisateur utilisateur = requireAdmin();
        if (properties != null) {
            for (AppPropertyDto dto : properties) {
                if (dto == null) {
                    continue;
                }
                appProperyService.save(dto.getKey(), dto.getValue(), utilisateur.getLogin());
            }
        }
        List<AppProperty> saved = appProperyService.getAll();
        return saved.stream().map(this::toDto).collect(Collectors.toList());
    }

    private AppPropertyDto toDto(AppProperty prop) {
        AppPropertyDto dto = new AppPropertyDto();
        dto.setKey(prop.getKey());
        dto.setValue(prop.getValue());
        return dto;
    }

    private Utilisateur requireAdmin() {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (utilisateur == null || !UtilisateurHelper.isAdmin(utilisateur)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Acces interdit");
        }
        return utilisateur;
    }
}
