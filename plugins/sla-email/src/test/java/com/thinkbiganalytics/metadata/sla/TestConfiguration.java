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

import com.thinkbiganalytics.common.velocity.service.InMemoryVelocityTemplateProvider;
import com.thinkbiganalytics.common.velocity.service.VelocityTemplateProvider;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Spring configuration to test the email notifications
 * This is used in the {@link TestEmail} class
 * Override the settings in the {@link #emailConfiguration} method to setup this test to work with your email server
 */
@Configuration
public class TestConfiguration {


    @Bean
    public PropertyPlaceholderConfigurer jiraPropertiesConfigurer() {
        PropertyPlaceholderConfigurer configurer = new
            PropertyPlaceholderConfigurer();
        configurer.setLocations(new ClassPathResource("/conf/sla.email.properties"));
        configurer.setIgnoreUnresolvablePlaceholders(true);
        configurer.setIgnoreResourceNotFound(true);
        return configurer;
    }

    @Bean
    public EmailServiceLevelAgreementAction emailServiceLevelAgreementAction() {
        return new EmailServiceLevelAgreementAction();
    }

    @Bean(name = "slaEmailConfiguration")
    public EmailConfiguration emailConfiguration() {
        EmailConfiguration emailConfiguration = new EmailConfiguration();
        emailConfiguration.setProtocol("smtp");
        emailConfiguration.setHost("smtp.gmail.com");
        emailConfiguration.setPort(587);
        //Note Google accounts will not allow overriding the from address due to security reasons.  Other accounts will.
        emailConfiguration.setFrom("sla-violation@thinkbiganalytics.com");
        emailConfiguration.setSmtpAuth("true");
        emailConfiguration.setStarttls("true");
        return emailConfiguration;
    }

    @Bean(name = "slaEmailSender")
    public JavaMailSender javaMailSender(@Qualifier("slaEmailConfiguration") EmailConfiguration emailConfiguration) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        Properties mailProperties = new Properties();
        mailProperties.put("mail.smtp.auth", emailConfiguration.getSmtpAuth());
        mailProperties.put("mail.smtp.starttls.enable", emailConfiguration.getStarttls());
        if (StringUtils.isNotBlank(emailConfiguration.getSmptAuthNtmlDomain())) {
            mailProperties.put("mail.smtp.auth.ntlm.domain", emailConfiguration.getSmptAuthNtmlDomain());
        }
        mailProperties.put("mail.debug", "true");

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

    @Bean
    public VelocityTemplateProvider velocityTemplateProvider(){
        return new InMemoryVelocityTemplateProvider();
    }

}
