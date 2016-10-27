package com.thinkbiganalytics.servicemonitor.rest.client.cdh;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.servicemonitor.rest.client.RestClientConfig;

/**
 * Created by sr186054 on 10/1/15.
 */
@Configuration
public class ClouderaClientConfig {

  @Bean(name = "clouderaRestClientConfig")
  @ConfigurationProperties("ambariRestClientConfig")
  public RestClientConfig getConfig() {
    return new RestClientConfig();
  }

}
