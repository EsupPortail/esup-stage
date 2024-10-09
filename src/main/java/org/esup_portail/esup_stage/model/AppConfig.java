package org.esup_portail.esup_stage.model;

import org.esup_portail.esup_stage.enums.AppConfigCodeEnum;

import jakarta.persistence.*;

@Entity
@Table(name = "AppConfig")
public class AppConfig {
    @Enumerated(EnumType.STRING)
    @Id
    @Column(name = "codeAppConfig", nullable = false)
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
