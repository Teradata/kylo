package com.thinkbiganalytics.metadata.sla;

/*-
 * #%L
 * thinkbig-sla-email
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import javax.inject.Inject;
import javax.mail.MessagingException;

/**
 * Spring service that is used to send emails based upon the defined "slaEmailConfiguratoin" bean that is defined in the {@link com.thinkbiganalytics.metadata.sla.config.EmailServiceLevelAgreementSpringConfiguration}
 */
public class SlaEmailService {

    @Inject
    @Qualifier("slaEmailSender")
    private MailSender mailSender;

    @Inject
    @Qualifier("slaEmailConfiguration")
    private EmailConfiguration emailConfiguration;

    /**
     * Send an email
     * @param to the user(s) to send the email to
     * @param subject the subject of the email
     * @param body the email body
     */
    public void sendMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailConfiguration.getFrom());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    /**
     * validate the email connection
     *
     * @return {@code true} if valid, {@code false} if not valid
     */
    public boolean testConnection() throws MessagingException{
        Properties props =  ((JavaMailSenderImpl)mailSender).getSession().getProperties();
        ((JavaMailSenderImpl)mailSender).testConnection();
        return true;
    }

    /**
     *
     * @return {@code true} if the configuration is setup, {@code false} if not configured
     */
    public boolean isConfigured(){
        return emailConfiguration.isConfigured();
    }


}
