package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.dto.ConfigThemeDto;
import org.esup_portail.esup_stage.enums.AppConfigCodeEnum;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.enums.TypeCentreEnum;
import org.esup_portail.esup_stage.model.AppConfig;
import org.esup_portail.esup_stage.repository.AppConfigJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Date;

@ApiController
@RequestMapping("/config")
public class AppConfigController {

    @Autowired
    AppConfigJpaRepository appConfigJpaRepository;

    @Autowired
    AppConfigService appConfigService;

    @GetMapping("/generale")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ConfigGeneraleDto getConfigGenerale() {
        return appConfigService.getConfigGenerale();
    }

    @PostMapping("/generale")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public ConfigGeneraleDto updateGenerale(@RequestBody ConfigGeneraleDto configGeneraleDto) throws JsonProcessingException {
        AppConfig appConfig = appConfigJpaRepository.findByCode(AppConfigCodeEnum.GENERAL);
        if (appConfig == null) {
            appConfig = new AppConfig();
            appConfig.setCode(AppConfigCodeEnum.GENERAL);
        }
        if (configGeneraleDto.getTypeCentre() == TypeCentreEnum.VIDE) {
            configGeneraleDto.setTypeCentre(null);
        }
        ObjectMapper mapper = new ObjectMapper();
        appConfig.setParametres(mapper.writeValueAsString(configGeneraleDto));
        appConfigJpaRepository.saveAndFlush(appConfig);
        return appConfigService.getConfigGenerale();
    }

    @GetMapping("/alerte-mail")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ConfigAlerteMailDto getConfigAlerteMail() {
        return appConfigService.getConfigAlerteMail();
    }

    @PostMapping("/alerte-mail")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public ConfigAlerteMailDto updateAlerteMail(@RequestBody ConfigAlerteMailDto configAlerteMailDto) throws JsonProcessingException {
        AppConfig appConfig = appConfigJpaRepository.findByCode(AppConfigCodeEnum.ALERTE);
        if (appConfig == null) {
            appConfig = new AppConfig();
            appConfig.setCode(AppConfigCodeEnum.ALERTE);
        }
        ObjectMapper mapper = new ObjectMapper();
        appConfig.setParametres(mapper.writeValueAsString(configAlerteMailDto));
        appConfigJpaRepository.saveAndFlush(appConfig);
        return appConfigService.getConfigAlerteMail();
    }

    @GetMapping("/theme")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.LECTURE})
    public ConfigThemeDto getConfigTheme() {
        return appConfigService.getConfigTheme();
    }

    @PostMapping("/theme")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.MODIFICATION})
    public ConfigThemeDto updateTheme(@RequestParam String data, @RequestParam(required = false) MultipartFile logo, @RequestParam(required = false) MultipartFile favicon) throws IOException, URISyntaxException {
        ObjectMapper mapper = new ObjectMapper();
        ConfigThemeDto configThemeDto = mapper.readValue(data, ConfigThemeDto.class);
        ConfigThemeDto configThemeDtoOrigin = appConfigService.getConfigTheme();
        AppConfig appConfig = appConfigJpaRepository.findByCode(AppConfigCodeEnum.THEME);
        if (appConfig == null) {
            appConfig = new AppConfig();
            appConfig.setCode(AppConfigCodeEnum.THEME);
        }
        configThemeDto.setDateModification(new Date());

        if (logo != null) {
            ConfigThemeDto.File64 logo64 = new ConfigThemeDto.File64();
            logo64.setContentType(logo.getContentType());
            logo64.setBase64(Base64.getEncoder().encodeToString(logo.getBytes()));
            configThemeDto.setLogo(logo64);
        } else {
            configThemeDto.setLogo(configThemeDtoOrigin.getLogo());
        }

        if (favicon != null) {
            ConfigThemeDto.File64 favicon64 = new ConfigThemeDto.File64();
            favicon64.setContentType(favicon.getContentType());
            favicon64.setBase64(Base64.getEncoder().encodeToString(favicon.getBytes()));
            configThemeDto.setFavicon(favicon64);
        } else {
            configThemeDto.setFavicon(configThemeDtoOrigin.getFavicon());
        }

        appConfig.setParametres(mapper.writeValueAsString(configThemeDto));
        appConfigJpaRepository.saveAndFlush(appConfig);
        appConfigService.updateTheme();

        return appConfigService.getConfigTheme();
    }

    @DeleteMapping("/theme")
    @Secure(fonctions = {AppFonctionEnum.PARAM_GLOBAL}, droits = {DroitEnum.SUPPRESSION})
    public ConfigThemeDto rollbackTheme() throws IOException, URISyntaxException {
        AppConfig appConfig = appConfigJpaRepository.findByCode(AppConfigCodeEnum.THEME);
        if (appConfig != null) {
            appConfigJpaRepository.delete(appConfig);
            appConfigJpaRepository.flush();
        }
        appConfigService.updateTheme();
        return appConfigService.getConfigTheme();
    }
}
