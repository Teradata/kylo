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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import com.google.common.base.Throwables;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Spring service that is used to send emails based upon the defined "slaEmailConfiguration" bean that is defined in the {@link com.thinkbiganalytics.metadata.sla.config.EmailServiceLevelAgreementSpringConfiguration}
 */
public class SlaEmailService {

    private static final Logger log = LoggerFactory.getLogger(SlaEmailService.class);
    @Inject
    @Qualifier("slaEmailSender")
    private JavaMailSender mailSender;

    @Inject
    @Qualifier("slaEmailConfiguration")
    private EmailConfiguration emailConfiguration;

    /**
     * Send an email
     *
     * @param to      the user(s) to send the email to
     * @param subject the subject of the email
     * @param body    the email body
     */
    public void sendMail(String to, String subject, String body) {

        try {
            if (testConnection()) {
                MimeMessage message = mailSender.createMimeMessage();
                String fromAddress = StringUtils.defaultIfBlank(emailConfiguration.getFrom(), emailConfiguration.getUsername());
                message.setFrom(new InternetAddress(fromAddress));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                log.debug("Email send to {}", to);
            }
        } catch (MessagingException ex) {
            log.error("Exception while sending mail : {}", ex.getMessage());
            Throwables.propagate(ex);

        }
    }

    /**
     * validate the email connection
     *
     * @return {@code true} if valid, {@code false} if not valid
     */
    public boolean testConnection() throws MessagingException {
        ((JavaMailSenderImpl) mailSender).testConnection();
        return true;
    }

    /**
     * @return {@code true} if the configuration is setup, {@code false} if not configured
     */
    public boolean isConfigured() {
        return emailConfiguration.isConfigured();
    }


}
