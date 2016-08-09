package com.thinkbiganalytics.metadata.sla.config;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

/**
 * Created by sr186054 on 8/9/16.
 */
@Configuration
@Profile("developer.jira")
public class DeveloperJiraConfiguration {


    @Bean(name = "jiraProperties")
    public PropertyPlaceholderConfigurer jiraPropertiesConfigurer() {
        PropertyPlaceholderConfigurer configurer = new
            PropertyPlaceholderConfigurer();
        configurer.setLocations(new ClassPathResource("/conf/jira.properties"));
        configurer.setIgnoreUnresolvablePlaceholders(true);
        configurer.setIgnoreResourceNotFound(true);
        return configurer;
    }


}

