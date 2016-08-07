package com.thinkbiganalytics.metadata.sla.config;

import com.thinkbiganalytics.metadata.sla.JiraServiceLevelAgreementAction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 8/7/16.
 */
@Configuration
public class JiraSpringConfiguration {

    @Bean
    public JiraServiceLevelAgreementAction jiraServiceLevelAgreementAction() {
        return new JiraServiceLevelAgreementAction();
    }

}
