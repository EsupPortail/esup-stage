package fr.dauphine.estage.model;

import fr.dauphine.estage.enums.AppConfigCodeEnum;

import javax.persistence.*;

@Entity
@Table(name = "AppConfig")
public class AppConfig {
    @Enumerated(EnumType.STRING)
    @Id
    @Column(name = "codeAppConfig", nullable = false, length = 20)
    private AppConfigCodeEnum code;

    @Lob
    @Column(nullable = false)
    private String parametres;

    public AppConfigCodeEnum getCode() {
        return code;
    }

    public void setCode(AppConfigCodeEnum code) {
        this.code = code;
    }

    public String getParametres() {
        return parametres;
    }

    public void setParametres(String parametres) {
        this.parametres = parametres;
    }
}
