/**
 *
 */
package com.thinkbiganalytics.metadata.jpa;

import javax.sql.DataSource;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.thinkbiganalytics.alerts.api.core.AggregatingAlertProvider;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.core.InMemorySLAProvider;

import reactor.bus.EventBus;

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
    
    @Bean(name = "alertsEventBus")
    @Primary
    public EventBus alertsEventBus() {
        return Mockito.mock(EventBus.class);
    }
    
    @Bean(name = "respondableAlertsEventBus")
    @Primary
    public EventBus respondableAlertsEventBus() {
        return Mockito.mock(EventBus.class);
    }
}
