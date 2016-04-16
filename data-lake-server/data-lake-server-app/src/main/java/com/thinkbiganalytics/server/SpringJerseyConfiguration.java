package com.thinkbiganalytics.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 4/13/16.
 */
@Configuration
public class SpringJerseyConfiguration {

  @Bean
  public JerseyConfig jerseyConfig() {
    return new JerseyConfig();
  }
}
