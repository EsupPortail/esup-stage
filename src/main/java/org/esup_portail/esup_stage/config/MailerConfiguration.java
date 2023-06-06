package org.esup_portail.esup_stage.config;

import freemarker.template.TemplateExceptionHandler;
import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.Properties;

@Configuration
public class MailerConfiguration {

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        String protocol = applicationBootstrap.getAppConfig().getMailerProtocol();
        mailSender.setProtocol(protocol);
        mailSender.setHost(applicationBootstrap.getAppConfig().getMailerHost());
        mailSender.setPort(applicationBootstrap.getAppConfig().getMailerPort());
        if (applicationBootstrap.getAppConfig().getMailerAuth()) {
            mailSender.setUsername(applicationBootstrap.getAppConfig().getMailerUsername());
            mailSender.setPassword(applicationBootstrap.getAppConfig().getMailerPassword());
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail." + protocol + ".auth", applicationBootstrap.getAppConfig().getMailerAuth());
        props.put("mail." + protocol + ".from", applicationBootstrap.getAppConfig().getMailerFrom());
        if (applicationBootstrap.getAppConfig().isMailerSsl()) {
            props.put("mail." + protocol + ".ssl.enable", applicationBootstrap.getAppConfig().isMailerSsl());
        }

        return mailSender;
    }

    @Bean
    public FreeMarkerConfigurer freemarkerClassLoaderConfig() {
        freemarker.template.Configuration configuration = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_27);
        configuration.setClassForTemplateLoading(this.getClass(), "/templates");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setConfiguration(configuration);
        return freeMarkerConfigurer;
    }
}
