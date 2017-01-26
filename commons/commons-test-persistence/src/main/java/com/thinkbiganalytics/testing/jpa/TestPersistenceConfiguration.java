/**
 *
 */
package com.thinkbiganalytics.testing.jpa;

/*-
 * #%L
 * thinkbig-commons-test-persistence
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

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.thinkbiganalytics.metadata.jpa"}, transactionManagerRef = "operationalMetadataTransactionManager",
                       entityManagerFactoryRef = "operationalMetadataEntityManagerFactory")
public class TestPersistenceConfiguration {


    /**
     * This is the datasource used by JPA
     */
    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource", locations = "classpath:test-jpa-application.properties")
    public DataSource dataSource() {
        DataSource newDataSource = DataSourceBuilder.create().build();

        return newDataSource;
    }

}
