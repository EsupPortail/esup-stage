package org.esup_portail.esup_stage.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;

@ApiController
public class ConfigurationController {

    @Value("${application.version}")
    private String version;

    @GetMapping("/version")
    public String version() {
        return version;
    }
}
