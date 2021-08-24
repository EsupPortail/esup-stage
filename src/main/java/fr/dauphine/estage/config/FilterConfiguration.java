package fr.dauphine.estage.config;

import fr.dauphine.estage.security.CasFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfiguration {

    @Autowired
    CasFilter casFilter;

    @Bean
    public FilterRegistrationBean<CasFilter> filterRegistrationBean() {
        FilterRegistrationBean<CasFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(casFilter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.addUrlPatterns("/frontend/*");

        return registrationBean;
    }
}
