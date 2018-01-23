package com.thinkbiganalytics.metadata.sla.config;

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


import com.thinkbiganalytics.metadata.sla.EmailConfiguration;
import com.thinkbiganalytics.metadata.sla.EmailServiceLevelAgreementAction;
import com.thinkbiganalytics.metadata.sla.SlaEmailService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Spring configuration creating the SLA email spring beans
 */
@Configuration
@PropertySource(value = "classpath:sla.email.properties", ignoreResourceNotFound = true)
public class EmailServiceLevelAgreementSpringConfiguration {


    @Bean
    public EmailServiceLevelAgreementAction emailServiceLevelAgreementAction() {
        return new EmailServiceLevelAgreementAction();
    }

    @Bean(name = "slaEmailConfiguration")
    @ConfigurationProperties(prefix = "sla.mail")
    public EmailConfiguration emailConfiguration() {
        return new EmailConfiguration();
    }


    @Bean(name = "slaEmailSender")
    public JavaMailSender javaMailSender(@Qualifier("slaEmailConfiguration") EmailConfiguration emailConfiguration) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        Properties mailProperties = new Properties();
        mailProperties.put("mail.smtp.auth", ""+emailConfiguration.isSmtpAuth());
        mailProperties.put("mail.smtp.starttls.enable", ""+emailConfiguration.isStarttls());
        if (StringUtils.isNotBlank(emailConfiguration.getSmptAuthNtmlDomain())) {
            mailProperties.put("mail.smtp.auth.ntlm.domain", emailConfiguration.getSmptAuthNtmlDomain());
        }
        mailProperties.put("mail.smtp.connectiontimeout ", "5000");
        mailProperties.put("mail.smtp.timeout", "5000");
        mailProperties.put("mail.smtp.writetimeout", "5000");
        if (emailConfiguration.isSslEnable()) {
            mailProperties.put("mail.smtp.ssl.enable", "true");
            mailProperties.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        }
        mailProperties.put("mail.debug", emailConfiguration.isDebug());

        mailSender.setJavaMailProperties(mailProperties);
        mailSender.setHost(emailConfiguration.getHost());
        mailSender.setPort(emailConfiguration.getPort());
        mailSender.setProtocol(emailConfiguration.getProtocol());
        mailSender.setUsername(emailConfiguration.getUsername());
        mailSender.setPassword(emailConfiguration.getPassword());
        return mailSender;
    }

    @Bean
    public SlaEmailService slaEmailService() {
        return new SlaEmailService();
    }


}
