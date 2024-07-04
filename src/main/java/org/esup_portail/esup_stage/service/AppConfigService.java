package org.esup_portail.esup_stage.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.dto.ConfigThemeDto;
import org.esup_portail.esup_stage.enums.AppConfigCodeEnum;
import org.esup_portail.esup_stage.enums.FolderEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Affectation;
import org.esup_portail.esup_stage.model.AppConfig;
import org.esup_portail.esup_stage.repository.AffectationRepository;
import org.esup_portail.esup_stage.repository.AppConfigJpaRepository;
import org.esup_portail.esup_stage.security.ApplicationStartUp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

@Service
public class AppConfigService {
    private static final Logger logger	= LogManager.getLogger(AppConfigService.class);

    @Autowired
    AppConfigJpaRepository appConfigJpaRepository;

    @Autowired
    AffectationRepository affectationRepository;

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    public ConfigGeneraleDto getConfigGenerale() {
        AppConfig appConfig = appConfigJpaRepository.findByCode(AppConfigCodeEnum.GENERAL);
        if (appConfig == null) {
            return new ConfigGeneraleDto();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ConfigGeneraleDto configGeneraleDto = objectMapper.readValue(appConfig.getParametres(), ConfigGeneraleDto.class);
            configGeneraleDto.setSignatureType(applicationBootstrap.getAppConfig().getAppSignatureEnabled());
            configGeneraleDto.setSignatureEnabled(configGeneraleDto.getSignatureType() != null);
            // Initialisation du code université si non renseigné
            if (configGeneraleDto.getCodeUniversite() == null || configGeneraleDto.getCodeUniversite().isEmpty()) {
                Affectation affectation = affectationRepository.getOneNotNullCodeUniversite();
                if (affectation != null) {
                    configGeneraleDto.setCodeUniversite(affectation.getId().getCodeUniversite());
                }
            }
            return configGeneraleDto;
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
            ConfigThemeDto configThemeDto = objectMapper.readValue(appConfig.getParametres(), ConfigThemeDto.class);
            if (configThemeDto.getLogo() != null && configThemeDto.getLogo().getContentType() != null) {
                File file = new File(applicationBootstrap.getAppConfig().getDataDir() + FolderEnum.IMAGES + "/" + getFilename("logo", configThemeDto.getLogo().getContentType()));
                if (file.exists()) {
                    configThemeDto.getLogo().setBase64(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(file)));
                }
            }
            if (configThemeDto.getFavicon() != null && configThemeDto.getFavicon().getContentType() != null) {
                File file = new File(applicationBootstrap.getAppConfig().getDataDir() + FolderEnum.IMAGES + "/" + getFilename("favicon", configThemeDto.getFavicon().getContentType()));
                if (file.exists()) {
                    configThemeDto.getFavicon().setBase64(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(file)));
                }
            }
            return configThemeDto;
        } catch (IOException e) {
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

            writeImageIntoFile(configThemeDto);
        } finally {
            file.flush();
            file.close();
        }
    }

    public String getAnneeUniv() {
        return getAnneeUniv(new Date());
    }

    public String getAnneeUniv(Date date) {
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);
        int year = dateCalendar.get(Calendar.YEAR);
        Calendar dateBascule = getDateBascule(year);

        return date.before(dateBascule.getTime()) ? String.valueOf(dateBascule.get(Calendar.YEAR) - 1) : String.valueOf(dateBascule.get(Calendar.YEAR));
    }

    public String getAnneeUnivLibelle(String annee) {
        return annee + "/" + (Integer.parseInt(annee) + 1);
    }

    public String getAnneeUnivFromLibelle(String anneeLibelle) {
        String[] parts = anneeLibelle.split("/");
        return parts[0];
    }

    public Calendar getDateBascule(int year) {
        ConfigGeneraleDto configGeneraleDto = getConfigGenerale();
        int jourBascule = configGeneraleDto.getAnneeBasculeJour();
        int moisBascule = configGeneraleDto.getAnneeBasculeMois();

        Calendar bascule = Calendar.getInstance();
        bascule.set(year, (moisBascule - 1), jourBascule, 0, 0);
        bascule.clear(Calendar.MILLISECOND);
        return bascule;
    }

    public void writeImageIntoFile(ConfigThemeDto configThemeDto) throws IOException {
        if (configThemeDto.getLogo() != null || configThemeDto.getFavicon() != null) {
            AppConfig appConfig = appConfigJpaRepository.findByCode(AppConfigCodeEnum.THEME);
            ObjectMapper mapper = new ObjectMapper();
            if (configThemeDto.getLogo() != null && configThemeDto.getLogo().getBase64() != null) {
                try (FileOutputStream outputStream = new FileOutputStream(applicationBootstrap.getAppConfig().getDataDir() + FolderEnum.IMAGES + "/" + getFilename("logo", configThemeDto.getLogo().getContentType()))) {
                    outputStream.write(Base64.getDecoder().decode(configThemeDto.getLogo().getBase64()));
                    configThemeDto.getLogo().setBase64(null);
                    appConfig.setParametres(mapper.writeValueAsString(configThemeDto));
                    appConfigJpaRepository.save(appConfig);
                }
            }
            if (configThemeDto.getFavicon() != null && configThemeDto.getFavicon().getBase64() != null) {
                try (FileOutputStream outputStream = new FileOutputStream(applicationBootstrap.getAppConfig().getDataDir() + FolderEnum.IMAGES + "/" + getFilename("favicon", configThemeDto.getFavicon().getContentType()))) {
                    outputStream.write(Base64.getDecoder().decode(configThemeDto.getFavicon().getBase64()));
                    configThemeDto.getFavicon().setBase64(null);
                    appConfig.setParametres(mapper.writeValueAsString(configThemeDto));
                    appConfigJpaRepository.save(appConfig);
                }
            }
        }
    }

    private String getFilename(String type, String contentType) {
        String extension = "";
        switch (contentType) {
            case "image/bmp":
                extension = ".bmp";
                break;
            case "image/gif":
                extension = ".gif";
                break;
            case "image/x-icon":
                extension = ".ico";
                break;
            case "image/jpeg":
                extension = ".jpeg";
                break;
            case "image/png":
                extension = ".png";
                break;
            case "image/svg+xml":
                extension = ".svg";
                break;
            case "image/tiff":
                extension = ".tiff";
                break;
            case "image/webp":
                extension = ".webp";
                break;
            default:
                break;
        }
        return type + extension;
    }
}
