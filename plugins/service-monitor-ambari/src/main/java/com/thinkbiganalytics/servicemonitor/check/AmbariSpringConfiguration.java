package com.thinkbiganalytics.servicemonitor.check;

import com.thinkbiganalytics.servicemonitor.rest.client.ambari.AmbariClient;
import com.thinkbiganalytics.servicemonitor.rest.client.ambari.AmbariJerseyClient;
import com.thinkbiganalytics.servicemonitor.rest.client.ambari.AmbariJerseyRestClientConfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
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


  @Bean(name = "ambariJerseyClientConfig")
  @ConfigurationProperties(prefix = "ambariRestClientConfig")
  public AmbariJerseyRestClientConfig ambariJerseyRestClientConfig() {
    return new AmbariJerseyRestClientConfig();
  }

  @Bean(name = "ambariClient")
  public AmbariClient ambariClient() {
    return new AmbariJerseyClient(ambariJerseyRestClientConfig());
  }
}
