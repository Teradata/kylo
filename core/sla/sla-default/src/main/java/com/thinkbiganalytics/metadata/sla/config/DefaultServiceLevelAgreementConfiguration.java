package com.thinkbiganalytics.metadata.sla.config;

import com.thinkbiganalytics.metadata.sla.alerts.EmailConfiguration;
import com.thinkbiganalytics.metadata.sla.alerts.EmailServiceLevelAgreementAction;
import com.thinkbiganalytics.metadata.sla.alerts.SlaEmailService;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.core.FeedOnTimeArrivalMetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.Serializable;
import java.util.Properties;

/**
 * Created by sr186054 on 7/22/16.
 */
@Configuration
public class DefaultServiceLevelAgreementConfiguration {


    @Bean(name = "onTimeAssessor")
    public MetricAssessor<? extends Metric, Serializable> onTimeMetricAssessor(@Qualifier("slaAssessor") ServiceLevelAssessor slaAssessor) {
        FeedOnTimeArrivalMetricAssessor metricAssr = new FeedOnTimeArrivalMetricAssessor();
        slaAssessor.registerMetricAssessor(metricAssr);
        return metricAssr;
    }

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
