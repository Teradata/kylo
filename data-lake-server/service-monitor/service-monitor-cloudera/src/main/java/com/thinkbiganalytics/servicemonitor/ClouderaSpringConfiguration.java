package com.thinkbiganalytics.servicemonitor;

import com.thinkbiganalytics.servicemonitor.check.ClouderaServicesStatusCheck;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 4/13/16.
 */
@Configuration
public class ClouderaSpringConfiguration {

    @Bean(name = "clouderaServicesStatusCheck")
    public ClouderaServicesStatusCheck clouderaServicesStatusCheck() {
        return new ClouderaServicesStatusCheck();
    }

}
