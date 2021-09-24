package fr.dauphine.estage.dto;

import fr.dauphine.estage.enums.AppConfigCodeEnum;
import fr.dauphine.estage.model.AppConfig;
import fr.dauphine.estage.model.Utilisateur;
import fr.dauphine.estage.model.helper.AppConfigHelper;

import java.util.List;

public class ContextDto {

    private Utilisateur utilisateur;
    private String authMode;
    private ConfigGeneraleDto configGenerale;
    private ConfigAlerteMailDto configAlerteMail;
    private ConfigThemeDto configTheme;

    public ContextDto() {
    }

    public ContextDto(Utilisateur utilisateur, String authMode, List<AppConfig> appConfigs) {
        this.utilisateur = utilisateur;
        this.authMode = authMode;
        this.configGenerale = AppConfigHelper.getConfigGenerale(appConfigs.stream().filter(a -> a.getCode() == AppConfigCodeEnum.GENERAL).findFirst().orElse(null));
        this.configAlerteMail = AppConfigHelper.getConfigAlerteMail(appConfigs.stream().filter(a -> a.getCode() == AppConfigCodeEnum.ALERTE).findFirst().orElse(null));
        this.configTheme = AppConfigHelper.getConfigTheme(appConfigs.stream().filter(a -> a.getCode() == AppConfigCodeEnum.THEME).findFirst().orElse(null));
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getAuthMode() {
        return authMode;
    }

    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }

    public ConfigGeneraleDto getConfigGenerale() {
        return configGenerale;
    }

    public void setConfigGenerale(ConfigGeneraleDto configGenerale) {
        this.configGenerale = configGenerale;
    }

    public ConfigAlerteMailDto getConfigAlerteMail() {
        return configAlerteMail;
    }

    public void setConfigAlerteMail(ConfigAlerteMailDto configAlerteMail) {
        this.configAlerteMail = configAlerteMail;
    }

    public ConfigThemeDto getConfigTheme() {
        return configTheme;
    }

    public void setConfigTheme(ConfigThemeDto configTheme) {
        this.configTheme = configTheme;
    }
}
