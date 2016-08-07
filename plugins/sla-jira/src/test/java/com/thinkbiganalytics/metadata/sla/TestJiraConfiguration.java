package com.thinkbiganalytics.metadata.sla;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;

/**
 * Created by sr186054 on 8/7/16.
 */
@Configuration
@ImportResource("classpath:/conf/jira-rest-client.xml")
public class TestJiraConfiguration {


    @Bean
    public PropertyPlaceholderConfigurer jiraPropertiesConfigurer() {
        PropertyPlaceholderConfigurer configurer = new
            PropertyPlaceholderConfigurer();
        configurer.setLocations(new ClassPathResource("jira.properties"));
        configurer.setIgnoreUnresolvablePlaceholders(true);
        configurer.setIgnoreResourceNotFound(true);
        return configurer;
    }

}
