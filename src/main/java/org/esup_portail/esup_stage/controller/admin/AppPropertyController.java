package org.esup_portail.esup_stage.controller.admin;

import org.esup_portail.esup_stage.controller.ApiController;
import org.esup_portail.esup_stage.dto.AppPropertyDto;
import org.esup_portail.esup_stage.dto.ConfigTestResultDto;
import org.esup_portail.esup_stage.dto.DocaposteTestRequestDto;
import org.esup_portail.esup_stage.dto.EsupSignatureTestRequestDto;
import org.esup_portail.esup_stage.dto.MailerTestRequestDto;
import org.esup_portail.esup_stage.dto.ReferentielTestRequestDto;
import org.esup_portail.esup_stage.dto.SireneTestRequestDto;
import org.esup_portail.esup_stage.dto.WebhookTestRequestDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.model.AppProperty;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AdminService;
import org.esup_portail.esup_stage.service.proprety.AppProperyService;
import org.esup_portail.esup_stage.service.proprety.ConfigMissingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    
    @Autowired
    private AdminService adminService;

    @GetMapping("/missing")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public Map<String, List<String>> getMissing() {
        adminService.requireAdmin();
        return Map.of("missing", configMissingService.getMissingKeys());
    }

    @GetMapping("/properties")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public List<AppPropertyDto> getAllProperties() {
        adminService.requireAdmin();
        List<AppProperty> properties = appProperyService.getAll();
        return properties.stream().map(this::toDto).toList();
    }

    @PutMapping("/properties")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public List<AppPropertyDto> updateProperties(@RequestBody List<AppPropertyDto> properties) {
        adminService.requireAdmin();
        if (properties != null) {
            for (AppPropertyDto dto : properties) {
                if (dto == null || !StringUtils.hasText(dto.getKey())) {
                    continue;
                }
                appProperyService.save(dto.getKey(), dto.getValue());
            }
        }
        return appProperyService.getAll().stream()
                .map(this::toDto)
                .toList();
    }

    @PostMapping("/test/mailer")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ConfigTestResultDto testMailer(@RequestBody MailerTestRequestDto request) {
        adminService.requireAdmin();
        return appProperyService.testMailer(request);
    }

    @PostMapping("/test/referentiel")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ConfigTestResultDto testReferentiel(@RequestBody ReferentielTestRequestDto request) {
        adminService.requireAdmin();
        return appProperyService.testReferentiel(request);
    }

    @PostMapping("/test/webhook")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ConfigTestResultDto testWebhook(@RequestBody WebhookTestRequestDto request) {
        adminService.requireAdmin();
        return appProperyService.testWebhook(request);
    }

    @PostMapping("/test/sirene")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ConfigTestResultDto testSirene(@RequestBody SireneTestRequestDto request) {
        adminService.requireAdmin();
        return appProperyService.testSirene(request);
    }

    @PostMapping("/test/docaposte")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ConfigTestResultDto testDocaposte(@RequestBody DocaposteTestRequestDto request) {
        adminService.requireAdmin();
        return appProperyService.testDocaposte(request);
    }

    @PostMapping("/test/esupsignature")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ConfigTestResultDto testEsupSignature(@RequestBody EsupSignatureTestRequestDto request) {
        adminService.requireAdmin();
        return appProperyService.testEsupSignature(request);
    }

    private AppPropertyDto toDto(AppProperty prop) {
        AppPropertyDto dto = new AppPropertyDto();
        dto.setKey(prop.getKey());
        dto.setIsSecret(Boolean.TRUE.equals(prop.getIsSecret()));
        if (Boolean.TRUE.equals(prop.getIsSecret())) {
            dto.setValue(null);
            dto.setHasValue(
                    StringUtils.hasText(prop.getValueEncrypted()) ||
                            StringUtils.hasText(prop.getValue())
            );
        } else {
            dto.setValue(prop.getValue());
            dto.setHasValue(StringUtils.hasText(prop.getValue()));
        }
        return dto;
    }

}
