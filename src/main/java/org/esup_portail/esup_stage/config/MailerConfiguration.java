package org.esup_portail.esup_stage.config;

import freemarker.template.TemplateExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.Properties;

@Configuration
public class MailerConfiguration {

    @Value("${appli.mailer.protocol}")
    private String mailerProtocol;

    @Value("${appli.mailer.host}")
    private String mailerHost;

    @Value("${appli.mailer.ssl.enable}")
    private boolean mailerSslEnable;

    @Value("${appli.mailer.port}")
    private int mailerPort;

    @Value("${appli.mailer.auth}")
    private boolean mailerAuth;

    @Value("${appli.mailer.username}")
    private String mailerUsername;

    @Value("${appli.mailer.password}")
    private String mailerPassword;

    @Value("${appli.mailer.from}")
    private String mailerFrom;


    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setProtocol(mailerProtocol);
        mailSender.setHost(mailerHost);
        mailSender.setPort(mailerPort);
        if (mailerAuth) {
            mailSender.setUsername(mailerUsername);
            mailSender.setPassword(mailerPassword);
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail." + mailerProtocol + ".auth", mailerAuth);
        props.put("mail." + mailerProtocol + ".from", mailerFrom);
        if (mailerSslEnable) {
            props.put("mail." + mailerProtocol + ".ssl.enable", mailerSslEnable);
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