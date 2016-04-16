package com.thinkbiganalytics.servicemonitor.rest.client.ambari;

import com.thinkbiganalytics.servicemonitor.rest.client.RestClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 10/1/15.
 */
@Configuration
public class AmbariClientConfig {

    @Bean(name = "ambariRestClientConfig")
    public RestClientConfig getConfig() {
        return new RestClientConfig();
    }

}
