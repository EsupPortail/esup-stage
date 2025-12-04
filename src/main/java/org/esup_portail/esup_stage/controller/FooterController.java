package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@ApiController
@RequestMapping("/footer")
public class FooterController {

    @Autowired
    private AppliProperties appliProperties;

    @GetMapping
    public AppliProperties.FooterProperties getFooterProperties() {
        return appliProperties.getFooter();
    }

    @GetMapping("/github")
    public String getGithubUrl() {
        return appliProperties.getFooter().getGithub();
    }

    @GetMapping("/site")
    public String getSiteUrl() {
        return appliProperties.getFooter().getSite();
    }

    @GetMapping("/support")
    public String getSupportUrl() {
        return appliProperties.getFooter().getSupport();
    }

    @GetMapping("/wiki")
    public String getWikiUrl() {
        return appliProperties.getFooter().getWiki();
    }
}
