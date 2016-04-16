package com.thinkbiganalytics.servicemonitor.rest.servicestatus;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 4/13/16.
 */
@Configuration
public class AmbariSpringConfiguration {

    @Bean(name = "ambariServicesStatus")
    public AmbariServicesStatusCheck ambariServicesStatus() {
        return new AmbariServicesStatusCheck();
    }

}
