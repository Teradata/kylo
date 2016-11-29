/**
 *
 */
package com.thinkbiganalytics.metadata.jpa;

import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.core.InMemorySLAProvider;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
@Configuration
public class TestJpaConfiguration {


    /**
     * This is the datasource used by JPA
     */
    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource", locations = "classpath:test-application.properties")
    public DataSource dataSource() {
        DataSource newDataSource = DataSourceBuilder.create().build();

        return newDataSource;
    }

    @Bean
    public ServiceLevelAgreementProvider slaProvider() {
        return new InMemorySLAProvider();
    }

}
