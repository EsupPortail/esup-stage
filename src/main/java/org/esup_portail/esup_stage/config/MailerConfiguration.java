package org.esup_portail.esup_stage.config;

import freemarker.template.TemplateExceptionHandler;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.Properties;

@Configuration
public class MailerConfiguration {

    final AppliProperties appliProperties;

    private final String mailerProtocol;

    public MailerConfiguration(AppliProperties appliProperties) {
        this.appliProperties = appliProperties;
        this.mailerProtocol = appliProperties.getMailer().getProtocol();
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setProtocol(mailerProtocol);
        mailSender.setHost(appliProperties.getMailer().getHost());
        mailSender.setPort(appliProperties.getMailer().getPort());
        if (appliProperties.getMailer().isAuth()) {
            mailSender.setUsername(appliProperties.getMailer().getUsername());
            mailSender.setPassword(appliProperties.getMailer().getPassword());
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail." + mailerProtocol + ".auth", appliProperties.getMailer().isAuth());
        props.put("mail." + mailerProtocol + ".from", appliProperties.getMailer().getFrom());
        // Support SSL/TLS direct (port 465 typiquement)
        if (appliProperties.getMailer().isSslEnable()) {
            props.put("mail." + mailerProtocol + ".ssl.enable", appliProperties.getMailer().isSslEnable());
        }
        // Support STARTTLS (port 587 typiquement)
        if (appliProperties.getMailer().isStarttlsEnable()) {
            props.put("mail." + mailerProtocol + ".starttls.enable", appliProperties.getMailer().isStarttlsEnable());
            if (appliProperties.getMailer().isStarttlsRequired()) {
                props.put("mail." + mailerProtocol + ".starttls.required", appliProperties.getMailer().isStarttlsRequired());
            }
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