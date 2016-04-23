package com.thinkbiganalytics.servicemonitor.db;

import com.thinkbiganalytics.servicemonitor.check.PipelineDatabaseServiceStatusCheck;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 4/13/16.
 */
@Configuration
public class PipelineDbSpringConfiguration {

  @Bean(name = "pipelineDatabaseServiceStatus")
  public PipelineDatabaseServiceStatusCheck pipelineDatabaseServiceHealth() {
    return new PipelineDatabaseServiceStatusCheck();
  }
}
