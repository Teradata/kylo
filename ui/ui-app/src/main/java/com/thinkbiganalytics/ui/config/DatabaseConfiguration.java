package com.thinkbiganalytics.ui.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;


@Configuration
public class DatabaseConfiguration {
  @Autowired
  private Environment environment;

  /**
   * Access to the jdbc template for persisting job executions
   *
   * @param dataSource The datasource injected from spring boot
   * @return The jdbc template
   */
  @Bean
  @Primary
  public JdbcTemplate jdbcTemplate(final DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  @Primary
  @ConfigurationProperties(prefix = "spring.datasource")
  public DataSource dataSource() {
    int i = 0;
    DataSource newDataSource = DataSourceBuilder.create().build();

    return newDataSource;
  }
}


