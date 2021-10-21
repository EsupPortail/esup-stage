package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

@ApiController
public class ConfigurationController {

    @Autowired
    private ApplicationBootstrap applicationBootstrap;

    @GetMapping("/version")
    public String testApp() {
        return applicationBootstrap.getApplicationVersion();
    }
}
