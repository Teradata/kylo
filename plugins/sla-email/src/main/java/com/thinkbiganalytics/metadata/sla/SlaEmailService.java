package com.thinkbiganalytics.metadata.sla;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import javax.inject.Inject;
import javax.mail.MessagingException;

/**
 * Created by sr186054 on 7/25/16.
 */
public class SlaEmailService {

    @Inject
    @Qualifier("slaEmailSender")
    private MailSender mailSender;

    @Inject
    @Qualifier("slaEmailConfiguration")
    private EmailConfiguration emailConfiguration;

    /**
     * This method will send compose and send the message
     */
    public void sendMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailConfiguration.getFrom());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public boolean testConnection() throws MessagingException{
      Properties props =  ((JavaMailSenderImpl)mailSender).getSession().getProperties();
        ((JavaMailSenderImpl)mailSender).testConnection();
        return true;
    }

    public boolean isConfigured(){
        return emailConfiguration.isConfigured();
    }


}