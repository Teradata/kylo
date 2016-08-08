package com.thinkbiganalytics.metadata.sla;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Created by sr186054 on 8/6/16.
 */
@Configuration
public class TestConfiguration {

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
        emailConfiguration.setSmtpAuth(true);
        emailConfiguration.setStarttls(true);
        //emailConfiguration.setSmptAuthNtmlDomain("td");
        emailConfiguration.setPassword("th1nkb1g");
        emailConfiguration.setUsername("thinkbig.tester@gmail.com");
        return emailConfiguration;
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
        mailProperties.put("mail.debug", "true");
        // mailProperties.put("mail.transport.protocol", emailConfiguration.getProtocol());
        //mailProperties.put("mail.smtp.connectiontimeout ",5000);
        //mailProperties.put("mail.smtp.timeout",5000);
        //mailProperties.put("mail.smtp.ssl.enable", "true");

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
