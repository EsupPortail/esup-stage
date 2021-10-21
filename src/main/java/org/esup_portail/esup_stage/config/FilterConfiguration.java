package org.esup_portail.esup_stage.config;

import org.esup_portail.esup_stage.security.filter.CasFilter;
import org.esup_portail.esup_stage.security.filter.CookieFilter;
import org.esup_portail.esup_stage.security.filter.LogoutFilter;
import org.esup_portail.esup_stage.security.filter.TokenFilter;
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

    @Autowired
    LogoutFilter logoutFilter;

    @Autowired
    TokenFilter tokenFilter;

    @Bean
    public FilterRegistrationBean<CasFilter> casFilterRegistrationBean() {
        FilterRegistrationBean<CasFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(casFilter);
        registrationBean.addUrlPatterns("/frontend/*");

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<CookieFilter> cookieFilterRegistrationBean() {
        FilterRegistrationBean<CookieFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(cookieFilter);
        registrationBean.addUrlPatterns("/frontend/*");

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<LogoutFilter> logoutFilterFilterRegistrationBean() {
        FilterRegistrationBean<LogoutFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(logoutFilter);
        registrationBean.addUrlPatterns("/logout");

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<TokenFilter> tokenFilterFilterRegistrationBean() {
        FilterRegistrationBean<TokenFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(tokenFilter);
        registrationBean.addUrlPatterns("/token/*");

        return registrationBean;
    }
}
