package fr.dauphine.estage.config;

import fr.dauphine.estage.security.CasFilter;
import fr.dauphine.estage.security.CookieFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfiguration {

    @Autowired
    CasFilter casFilter;

    @Autowired
    CookieFilter cookieFilter;

    @Bean
    public FilterRegistrationBean<CasFilter> casFilterRegistrationBean() {
        FilterRegistrationBean<CasFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(casFilter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.addUrlPatterns("/frontend/*");

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<CookieFilter> cookieFilterRegistrationBean() {
        FilterRegistrationBean<CookieFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(cookieFilter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.addUrlPatterns("/frontend/*");

        return registrationBean;
    }
}
