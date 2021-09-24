package fr.dauphine.estage.model.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dauphine.estage.dto.ConfigAlerteMailDto;
import fr.dauphine.estage.dto.ConfigGeneraleDto;
import fr.dauphine.estage.dto.ConfigThemeDto;
import fr.dauphine.estage.exception.AppException;
import fr.dauphine.estage.model.AppConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

public abstract class AppConfigHelper {
    private static final Logger logger	= LogManager.getLogger(AppConfigHelper.class);

    public static ConfigGeneraleDto getConfigGenerale(AppConfig appConfig) {
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

    public static ConfigAlerteMailDto getConfigAlerteMail(AppConfig appConfig) {
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

    public static ConfigThemeDto getConfigTheme(AppConfig appConfig) {
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
}
