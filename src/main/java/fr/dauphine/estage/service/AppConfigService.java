package fr.dauphine.estage.service;

import fr.dauphine.estage.dto.ConfigAlerteMailDto;
import fr.dauphine.estage.dto.ConfigGeneraleDto;
import fr.dauphine.estage.dto.ConfigThemeDto;
import fr.dauphine.estage.enums.AppConfigCodeEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.AppConfig;
import fr.dauphine.estage.model.helper.AppConfigHelper;
import fr.dauphine.estage.repository.AppConfigJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AppConfigService {

    @Autowired
    AppConfigJpaRepository appConfigJpaRepository;

    public ConfigGeneraleDto getConfigGenerale() {
        return AppConfigHelper.getConfigGenerale(getByCode(AppConfigCodeEnum.GENERAL));
    }

    public ConfigAlerteMailDto getConfigAlerte() {
        return AppConfigHelper.getConfigAlerteMail(getByCode(AppConfigCodeEnum.ALERTE));
    }

    public ConfigThemeDto getConfigTheme() {
        return AppConfigHelper.getConfigTheme(getByCode(AppConfigCodeEnum.THEME));
    }

    private AppConfig getByCode(AppConfigCodeEnum code) {
        return appConfigJpaRepository.findByCode(code);
    }
}
