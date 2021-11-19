package org.esup_portail.esup_stage.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.dto.ConfigThemeDto;
import org.esup_portail.esup_stage.enums.AppConfigCodeEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.AppConfig;
import org.esup_portail.esup_stage.repository.AppConfigJpaRepository;
import org.esup_portail.esup_stage.security.ApplicationStartUp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;

@Service
public class AppConfigService {
    private static final Logger logger	= LogManager.getLogger(AppConfigService.class);

    @Autowired
    AppConfigJpaRepository appConfigJpaRepository;

    public ConfigGeneraleDto getConfigGenerale() {
        AppConfig appConfig = appConfigJpaRepository.findByCode(AppConfigCodeEnum.GENERAL);
        if (appConfig == null) {
            return new ConfigGeneraleDto();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(appConfig.getParametres(), ConfigGeneraleDto.class);
        } catch (JsonProcessingException e) {
            logger.error(e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "ConfigGeneraleDto::getConfigGenerale ERROR: JsonProcessingException");
        }
    }

    public ConfigAlerteMailDto getConfigAlerteMail() {
        AppConfig appConfig = appConfigJpaRepository.findByCode(AppConfigCodeEnum.ALERTE);
        if (appConfig == null) {
            return new ConfigAlerteMailDto();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(appConfig.getParametres(), ConfigAlerteMailDto.class);
        } catch (JsonProcessingException e) {
            logger.error(e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "ConfigGeneraleDto::getConfigGenerale ERROR: JsonProcessingException");
        }
    }

    public ConfigThemeDto getConfigTheme() {
        AppConfig appConfig = appConfigJpaRepository.findByCode(AppConfigCodeEnum.THEME);
        if (appConfig == null) {
            return new ConfigThemeDto();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(appConfig.getParametres(), ConfigThemeDto.class);
        } catch (JsonProcessingException e) {
            logger.error(e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "ConfigGeneraleDto::getConfigGenerale ERROR: JsonProcessingException");
        }
    }

    public void updateTheme() throws IOException, URISyntaxException {
        FileWriter file = new FileWriter(ApplicationStartUp.class.getClassLoader().getResource("static/theme.css").getPath());
        try {
            ConfigThemeDto configThemeDto = getConfigTheme();
            StringBuilder sb = new StringBuilder();
            sb.append(":root {\n");
            sb.append("  --fontFamily: ").append(configThemeDto.getFontFamily()).append(";\n");
            sb.append("  --fontSize: ").append(configThemeDto.getFontSize()).append(";\n");
            sb.append("  --primaryColor: ").append(configThemeDto.getPrimaryColor()).append(";\n");
            sb.append("  --secondaryColor: ").append(configThemeDto.getSecondaryColor()).append(";\n");
            sb.append("  --dangerColor: ").append(configThemeDto.getDangerColor()).append(";\n");
            sb.append("  --warningColor: ").append(configThemeDto.getWarningColor()).append(";\n");
            sb.append("  --successColor: ").append(configThemeDto.getSuccessColor()).append(";\n");
            sb.append("}");
            file.write(sb.toString());
        } finally {
            file.flush();
            file.close();
        }
    }

    public String getAnneeUniv() {
        return getAnneeUniv(new Date());
    }

    public String getAnneeUniv(Date date) {
        ConfigGeneraleDto configGeneraleDto = getConfigGenerale();
        int jourBascule = configGeneraleDto.getAnneeBasculeJour();
        int moisBascule = configGeneraleDto.getAnneeBasculeMois();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        Calendar bascule = Calendar.getInstance();
        bascule.set(currentYear, (moisBascule - 1), jourBascule, 0, 0);
        bascule.clear(Calendar.MILLISECOND);
        Date dateBascule = bascule.getTime();

        return date.before(dateBascule) ? String.valueOf(currentYear) : String.valueOf(currentYear + 1);
    }

    public String getAnneeUnivLibelle(String annee) {
        return annee + "/" + (Integer.parseInt(annee) + 1);
    }

    public String getAnneeUnivFromLibelle(String anneeLibelle) {
        String[] parts = anneeLibelle.split("/");
        return parts[0];
    }
}
