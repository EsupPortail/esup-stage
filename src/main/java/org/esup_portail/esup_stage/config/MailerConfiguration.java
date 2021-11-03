package org.esup_portail.esup_stage.config;

import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

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

        return mailSender;
    }
}
