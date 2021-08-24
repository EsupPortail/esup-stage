package fr.dauphine.estage.bootstrap;

import java.util.Properties;

public class AppConfig {
    private String casUrlLogin;
    private String casUrlLogout;
    private String casUrlService;

    public String getCasUrlLogin() {
        return casUrlLogin;
    }

    public void setCasUrlLogin(String casUrlLogin) {
        this.casUrlLogin = casUrlLogin;
    }

    public String getCasUrlLogout() {
        return casUrlLogout;
    }

    public void setCasUrlLogout(String casUrlLogout) {
        this.casUrlLogout = casUrlLogout;
    }

    public String getCasUrlService() {
        return casUrlService;
    }

    public void setCasUrlService(String casUrlService) {
        this.casUrlService = casUrlService;
    }

    public void initProperties(Properties props, String prefixeProps) {
        this.casUrlLogout = props.getProperty("cas.url.logout");
        this.casUrlLogin = props.getProperty("cas.url.login");
        this.casUrlService = props.getProperty("cas.url.service");
    }

    @Override
    public String toString() {
        return "AppConfig{" +
                ", casUrlLogin='" + casUrlLogin + "'" +
                ", casUrlLogout='" + casUrlLogout + "'" +
                ", casUrlService='" + casUrlService + "'" +
                "}";
    }
}
