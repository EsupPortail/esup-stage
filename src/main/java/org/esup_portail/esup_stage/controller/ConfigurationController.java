package org.esup_portail.esup_stage.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;

@ApiController
public class ConfigurationController {

    @GetMapping("/version")
    public String testApp() {
        return "2.1.9";
    }
}
