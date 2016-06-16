package com.thinkbiganalytics.metadata.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Created by sr186054 on 6/15/16.
 */
@Configuration
public class DatabaseConfiguration {

    @Autowired
    public Environment env;


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
    @ConfigurationProperties(prefix = "spring.datasource", locations = "classpath:application.properties")
    public DataSource dataSource() {
        DataSource newDataSource = DataSourceBuilder.create().build();
        //    String url = env.getProperty("spring.datasource.url");
        return newDataSource;
    }


    @Bean
    public PropertyPlaceholderConfigurer propConfig() {
        PropertyPlaceholderConfigurer placeholderConfigurer = new PropertyPlaceholderConfigurer();
        placeholderConfigurer.setLocation(new ClassPathResource("application.properties"));
        return placeholderConfigurer;
    }

}
