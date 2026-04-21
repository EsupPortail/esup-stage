package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConfigThemeDto;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
public class ThemeCSSController {
    @Autowired
    AppConfigService appConfigService;

    @GetMapping(path="/theme.css", produces = "text/css")
    @ResponseBody
    public String themeCSS() throws IOException {
        return appConfigService.getThemeCss();
    }

}
