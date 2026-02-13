package org.esup_portail.esup_stage.config;

import freemarker.template.TemplateExceptionHandler;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.util.StringUtils;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(AppliProperties.class)
public class MailerConfiguration {

    private final AppliProperties appliProperties;

    public MailerConfiguration(AppliProperties appliProperties) {
        this.appliProperties = appliProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "appli.mailer", name = "host")
    public JavaMailSender getJavaMailSender() {
        var mailer = appliProperties.getMailer();
        String protocol = StringUtils.hasText(mailer.getProtocol()) ? mailer.getProtocol() : "smtp";
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setProtocol(protocol);
        mailSender.setHost(mailer.getHost());
        if (mailer.getPort() > 0) {
            mailSender.setPort(mailer.getPort());
        }
        if (mailer.isAuth()) {
            mailSender.setUsername(mailer.getUsername());
            mailSender.setPassword(mailer.getPassword());
        }
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail." + protocol + ".auth", String.valueOf(mailer.isAuth()));
        if (StringUtils.hasText(mailer.getFrom())) {
            props.put("mail." + protocol + ".from", mailer.getFrom());
        }
        if (mailer.isSslEnable()) {
            props.put("mail." + protocol + ".ssl.enable", "true");
        }
        return mailSender;
    }

    @Bean
    public FreeMarkerConfigurer freemarkerClassLoaderConfig() {
        freemarker.template.Configuration configuration =
                new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_27);
        configuration.setClassForTemplateLoading(this.getClass(), "/templates");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);

        FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setConfiguration(configuration);
        return freeMarkerConfigurer;
    }
}
