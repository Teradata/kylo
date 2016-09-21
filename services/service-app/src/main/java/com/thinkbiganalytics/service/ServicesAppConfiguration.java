package com.thinkbiganalytics.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 9/21/16.
 */
@Configuration
public class ServicesAppConfiguration {

    @Bean
    DefaultServicesApplicationStartup feedManagerApplicationStartup() {
        return new DefaultServicesApplicationStartup();
    }
}
