/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.jira.servicestatus;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 10/21/15.
 */
@Configuration
public class JiraServiceStatusConfig {

    @Bean(name="jiraServiceStatusCheck")
    public JiraServiceStatusCheck podiumServiceStatusCheck() {
        return new JiraServiceStatusCheck();
    }
}
