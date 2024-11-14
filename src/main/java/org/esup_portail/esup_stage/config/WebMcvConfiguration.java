package org.esup_portail.esup_stage.config;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.esup_portail.esup_stage.controller.ApiController;
import org.esup_portail.esup_stage.controller.apipublic.ApiPublicController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMcvConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/frontend/**")
                .addResourceLocations("classpath:/frontend/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/frontend");
        registry.addViewController("/frontend").setViewName("redirect:/frontend/");
        registry.addViewController("/frontend/").setViewName("forward:/frontend/index.html");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api", HandlerTypePredicate.forAnnotation(ApiController.class));
        configurer.addPathPrefix("/public/api", HandlerTypePredicate.forAnnotation(ApiPublicController.class));
    }

    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }
}
