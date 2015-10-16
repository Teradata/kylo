package com.thinkbiganalytics.jira;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Created by mk186074 on 10/13/15.
 */
@Configuration
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
@ImportResource("classpath:/conf/jira-rest-client.xml")
@PropertySource("classpath:jira.properties")
public class JiraSpringTestConfig {

    @Bean
    public PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }




}
