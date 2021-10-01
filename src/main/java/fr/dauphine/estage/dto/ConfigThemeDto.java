package fr.dauphine.estage.dto;

import java.util.Date;

public class ConfigThemeDto {
    private File64 logo;
    private File64 favicon;
    private String fontFamily = "Roboto, \"Helvetica Neue\", sans-serif";
    private String fontSize = "0.9rem";
    private String primaryColor = "#2e4588";
    private String secondaryColor = "#ff4081";
    private String dangerColor = "#e95160";
    private String warningColor = "#ffd046";
    private String successColor = "#adcd68";
    private Date dateModification = new Date();

    public File64 getLogo() {
        return logo;
    }

    public void setLogo(File64 logo) {
        this.logo = logo;
    }

    public File64 getFavicon() {
        return favicon;
    }

    public void setFavicon(File64 favicon) {
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

    public static class File64 {
        private String contentType;
        private String base64;

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getBase64() {
            return base64;
        }

        public void setBase64(String base64) {
            this.base64 = base64;
        }
    }
}
