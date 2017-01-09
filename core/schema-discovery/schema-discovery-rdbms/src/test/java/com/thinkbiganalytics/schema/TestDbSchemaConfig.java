package com.thinkbiganalytics.schema;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Created by sr186054 on 1/9/17.
 */
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
@Configuration
public class TestDbSchemaConfig {

    /**
     * This is the datasource used for testing
     */
    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource", locations = "classpath:test-application.properties")
    public DataSource dataSource() {
        DataSource newDataSource = DataSourceBuilder.create().build();

        return newDataSource;
    }

}
