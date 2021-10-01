package fr.dauphine.estage.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dauphine.estage.dto.ConfigAlerteMailDto;
import fr.dauphine.estage.dto.ConfigGeneraleDto;
import fr.dauphine.estage.dto.ConfigThemeDto;
import fr.dauphine.estage.enums.AppConfigCodeEnum;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.enums.TypeCentreEnum;
import fr.dauphine.estage.model.AppConfig;
import fr.dauphine.estage.repository.AppConfigJpaRepository;
import fr.dauphine.estage.security.interceptor.Secure;
import fr.dauphine.estage.service.AppConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

@ApiController
@RequestMapping("/config")
public class AppConfigController {

    @Autowired
    AppConfigJpaRepository appConfigJpaRepository;

    @Autowired
    AppConfigService appConfigService;

    @GetMapping("/generale")
    @Secure(fonction = AppFonctionEnum.PARAM_GLOBAL, droits = {DroitEnum.LECTURE})
    public ConfigGeneraleDto getConfigGenerale() {
        return appConfigService.getConfigGenerale();
    }

    @PostMapping("/generale")
    @Secure(fonction = AppFonctionEnum.PARAM_GLOBAL, droits = {DroitEnum.MODIFICATION})
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
    @Secure(fonction = AppFonctionEnum.PARAM_GLOBAL, droits = {DroitEnum.LECTURE})
    public ConfigAlerteMailDto getConfigAlerteMail() {
        return appConfigService.getConfigAlerteMail();
    }

    @PostMapping("/alerte-mail")
    @Secure(fonction = AppFonctionEnum.PARAM_GLOBAL, droits = {DroitEnum.MODIFICATION})
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
    @Secure(fonction = AppFonctionEnum.PARAM_GLOBAL, droits = {DroitEnum.LECTURE})
    public ConfigThemeDto getConfigTheme() {
        return appConfigService.getConfigTheme();
    }

    @PostMapping("/theme")
    @Secure(fonction = AppFonctionEnum.PARAM_GLOBAL, droits = {DroitEnum.MODIFICATION})
    public ConfigThemeDto updateTheme(@RequestBody ConfigThemeDto configAlerteMailDto) throws IOException, URISyntaxException {
        AppConfig appConfig = appConfigJpaRepository.findByCode(AppConfigCodeEnum.THEME);
        if (appConfig == null) {
            appConfig = new AppConfig();
            appConfig.setCode(AppConfigCodeEnum.THEME);
        }
        configAlerteMailDto.setDateModification(new Date());
        ObjectMapper mapper = new ObjectMapper();
        appConfig.setParametres(mapper.writeValueAsString(configAlerteMailDto));
        appConfigJpaRepository.saveAndFlush(appConfig);
        appConfigService.updateTheme();
        return appConfigService.getConfigTheme();
    }

    @DeleteMapping("/theme")
    @Secure(fonction = AppFonctionEnum.PARAM_GLOBAL, droits = {DroitEnum.SUPPRESSION})
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
