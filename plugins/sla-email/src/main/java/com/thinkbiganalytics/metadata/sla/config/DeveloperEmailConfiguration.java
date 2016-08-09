package com.thinkbiganalytics.metadata.sla.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by sr186054 on 8/9/16.
 */
@Configuration
@Profile("developer.email")
@PropertySource("classpath:/conf/sla.email.properties")
public class DeveloperEmailConfiguration {


/*
        @Bean(name = "emailProperties")
        public PropertyPlaceholderConfigurer jiraPropertiesConfigurer() {
            PropertyPlaceholderConfigurer configurer = new
                PropertyPlaceholderConfigurer();
            configurer.setLocations(new ClassPathResource("/conf/sla.email.properties"));
            configurer.setIgnoreUnresolvablePlaceholders(true);
            configurer.setIgnoreResourceNotFound(true);
            return configurer;
        }
*/


}
