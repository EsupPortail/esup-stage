package org.esup_portail.esup_stage.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;

@ApiController
public class ConfigurationController {

    @Value("${project.version}")
    private String appVersion;

    @GetMapping("/version")
    public String testApp() {
        return appVersion;
    }
}
