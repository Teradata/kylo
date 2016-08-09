package com.thinkbiganalytics.metadata.sla.config;

import com.thinkbiganalytics.metadata.sla.JiraServiceLevelAgreementAction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by sr186054 on 8/7/16.
 */
@Configuration
@ImportResource("classpath:/conf/jira-rest-client.xml")
@PropertySource(value = "classpath:jira.properties", ignoreResourceNotFound = true)
public class JiraSpringConfiguration {

    @Bean
    public JiraServiceLevelAgreementAction jiraServiceLevelAgreementAction() {
        return new JiraServiceLevelAgreementAction();
    }


}
