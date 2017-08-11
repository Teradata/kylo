/**
 *
 */
package com.thinkbiganalytics.metadata.jpa;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.core.InMemorySLAProvider;
import com.thinkbiganalytics.security.AccessController;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

import reactor.bus.EventBus;

@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
@Configuration
public class TestJpaConfiguration {

    @Bean
    AccessController accessController() {
        return Mockito.mock(AccessController.class);
    }


    @Bean(name = "kyloAlertManager")
    AlertManager alertManager() {
        return Mockito.mock(AlertManager.class);
    }

    @Bean
    AlertProvider alertProvider() {
        return Mockito.mock(AlertProvider.class);
    }

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
