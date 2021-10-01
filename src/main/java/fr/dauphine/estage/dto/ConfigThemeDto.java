package fr.dauphine.estage.dto;

import java.util.Date;

public class ConfigThemeDto {
    private String logo;
    private String favicon;
    private String fontFamily = "Roboto, \"Helvetica Neue\", sans-serif";
    private String fontSize = "0.9rem";
    private String primaryColor = "#2e4588";
    private String secondaryColor = "#ff4081";
    private String dangerColor = "#e95160";
    private String warningColor = "#ffd046";
    private String successColor = "#adcd68";
    private Date dateModification = new Date();

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getDangerColor() {
        return dangerColor;
    }

    public void setDangerColor(String dangerColor) {
        this.dangerColor = dangerColor;
    }

    public String getWarningColor() {
        return warningColor;
    }

    public void setWarningColor(String warningColor) {
        this.warningColor = warningColor;
    }

    public String getSuccessColor() {
        return successColor;
    }

    public void setSuccessColor(String successColor) {
        this.successColor = successColor;
    }

    public Date getDateModification() {
        return dateModification;
    }

    public void setDateModification(Date dateModification) {
        this.dateModification = dateModification;
    }
}
