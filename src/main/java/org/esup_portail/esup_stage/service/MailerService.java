package org.esup_portail.esup_stage.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.bootstrap.ApplicationBootstrap;
import org.esup_portail.esup_stage.exception.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class MailerService {
    private static final Logger logger	= LogManager.getLogger(MailerService.class);

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @Autowired
    JavaMailSender javaMailSender;

    public void sendMail(String to, String subject, String body) {
        boolean disableDelivery = applicationBootstrap.getAppConfig().getMailerDisableDelivery();
        if (!disableDelivery) {
            String deliveryAddress = applicationBootstrap.getAppConfig().getMailerDeliveryAddress();
            if (deliveryAddress != null && !deliveryAddress.isEmpty()) {
                to = deliveryAddress;
            }
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            try {
                helper.setTo(to);
                helper.setFrom(applicationBootstrap.getAppConfig().getMailerFrom());
                helper.setSubject(subject);
                helper.setText(body, true);
                javaMailSender.send(message);
            } catch (MessagingException e) {
                logger.error("Une erreur est survenue lors de l'envoi d'un email", e);
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur technique");
            }
        } else {
            logger.info("Delivery disabled. Mail was: " + body);
        }
    }
}
