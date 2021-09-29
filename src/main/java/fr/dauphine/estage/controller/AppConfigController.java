package fr.dauphine.estage.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dauphine.estage.dto.ConfigGeneraleDto;
import fr.dauphine.estage.dto.ContextDto;
import fr.dauphine.estage.enums.AppConfigCodeEnum;
import fr.dauphine.estage.enums.AppFonctionEnum;
import fr.dauphine.estage.enums.DroitEnum;
import fr.dauphine.estage.enums.TypeCentreEnum;
import fr.dauphine.estage.model.AppConfig;
import fr.dauphine.estage.model.helper.AppConfigHelper;
import fr.dauphine.estage.repository.AppConfigJpaRepository;
import fr.dauphine.estage.security.ServiceContext;
import fr.dauphine.estage.security.interceptor.Secure;
import fr.dauphine.estage.service.AppConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public Object update(@RequestBody ConfigGeneraleDto configGeneraleDto) throws JsonProcessingException {
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
        appConfig = appConfigJpaRepository.saveAndFlush(appConfig);
        return AppConfigHelper.getConfigGenerale(appConfig);
    }
}
