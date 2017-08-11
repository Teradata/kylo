package com.thinkbiganalytics.server;

/*-
 * #%L
 * thinkbig-service-app
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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.sql.DataSource;


@Configuration
public class DatabaseConfiguration {


    /**
     * This is the datasource used by JPA
     */
    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        DataSource newDataSource = DataSourceBuilder.create().build();

        return newDataSource;
    }

    /**
     * Access to the jdbc template for querying operational Data     *
     *
     * @param dataSource The datasource  from the {@see #jdbcDataSource()}
     * @return The jdbc template
     */
    @Bean
    @Primary
    public JdbcTemplate jdbcTemplate(@Qualifier("dataSource") final DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public FilterRegistrationBean openEntityManagerInViewFilter() {
        FilterRegistrationBean reg = new FilterRegistrationBean();
        reg.setName("OpenEntityManagerInViewFilter");
        reg.setFilter(new OpenEntityManagerInViewFilter());
        return reg;
    }

    /**
     * This the datasource used by the jdbcTemplate NOTE:  This datasource will be removed along with the {@see #jdbcTemplate(Datasource)}
     *
     * @return the JDBC Datasource
     * @Bean(name = "jdbcDataSource")
     * @ConfigurationProperties(prefix = "spring.datasource") public DataSource jdbcDataSource() { DataSource newDataSource = DataSourceBuilder.create().build();
     *
     * return newDataSource; }
     */

    @Configuration
    @ConditionalOnWebApplication
    @ConditionalOnClass(WebMvcConfigurerAdapter.class)
    @ConditionalOnMissingBean({OpenEntityManagerInViewInterceptor.class,
                               OpenEntityManagerInViewFilter.class})
    @ConditionalOnProperty(prefix = "spring.jpa", name = "open-in-view", havingValue = "true", matchIfMissing = true)
    protected static class JpaWebConfiguration {

        // Defined as a nested config to ensure WebMvcConfigurerAdapter is not read when
        // not on the classpath
        @Configuration
        protected static class JpaWebMvcConfiguration extends WebMvcConfigurerAdapter {

            @Bean
            public OpenEntityManagerInViewInterceptor openEntityManagerInViewInterceptor() {
                return new OpenEntityManagerInViewInterceptor();
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addWebRequestInterceptor(openEntityManagerInViewInterceptor());
            }

        }

    }


}


