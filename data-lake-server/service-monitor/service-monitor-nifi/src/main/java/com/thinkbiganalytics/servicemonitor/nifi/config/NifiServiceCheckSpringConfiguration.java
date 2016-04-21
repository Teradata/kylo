package com.thinkbiganalytics.servicemonitor.nifi.config;

import com.thinkbiganalytics.servicemonitor.nifi.NifiServiceStatusCheck;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 4/13/16.
 */
@Configuration
public class NifiServiceCheckSpringConfiguration {

  @Bean(name = "nifiServiceStatus")
  public NifiServiceStatusCheck pipelineDatabaseServiceHealth() {
    return new NifiServiceStatusCheck();
  }
}
