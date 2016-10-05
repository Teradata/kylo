package com.thinkbiganalytics.metadata.sla.config;


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
 * Created by sr186054 on 7/22/16.
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
        mailProperties.put("mail.smtp.auth", emailConfiguration.isSmtpAuth());
        mailProperties.put("mail.smtp.starttls.enable", emailConfiguration.isStarttls());
        if (StringUtils.isNotBlank(emailConfiguration.getSmptAuthNtmlDomain())) {
            mailProperties.put("mail.smtp.auth.ntlm.domain", emailConfiguration.getSmptAuthNtmlDomain());
        }
        mailProperties.put("mail.smtp.connectiontimeout ", "5000");
        mailProperties.put("mail.smtp.timeout", "5000");
        mailProperties.put("mail.smtp.writetimeout", "5000");
        if (emailConfiguration.isSslEnable()) {
            mailProperties.put("mail.smtp.ssl.enable", "true");
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
