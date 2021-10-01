package fr.dauphine.estage.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dauphine.estage.dto.ConfigAlerteMailDto;
import fr.dauphine.estage.dto.ConfigGeneraleDto;
import fr.dauphine.estage.dto.ConfigThemeDto;
import fr.dauphine.estage.enums.AppConfigCodeEnum;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.AppConfig;
import fr.dauphine.estage.repository.AppConfigJpaRepository;
import fr.dauphine.estage.security.ApplicationStartUp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

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
}
