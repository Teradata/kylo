package com.thinkbiganalytics.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 7/22/16.
 */
@Configuration
public class CommonsSpringConfiguration {

    @Bean
    public FileResourceService fileResourceService() {
        return new FileResourceService();
    }

}
