package com.thinkbiganalytics.hive.config;

/*-
 * #%L
 * kylo-thrift-proxy-core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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



import com.thinkbiganalytics.UsernameCaseStrategyUtil;
import com.thinkbiganalytics.hive.service.HiveService;
import com.thinkbiganalytics.hive.service.RefreshableDataSource;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 */
@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = {"com.thinkbiganalytics.hive.service"})
public class HiveDataSourceConfiguration {

    @Autowired
    private Environment env;

    @Autowired
    private KerberosTicketConfiguration kerberosHiveConfiguration;

    @Autowired
    private UsernameCaseStrategyUtil usernameCaseStrategyUtil;

    @Bean(name = "hiveJdbcTemplate")
    public JdbcTemplate hiveJdbcTemplate(@Qualifier("hiveDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


    @Bean(name = "hiveMetatoreJdbcTemplate")
    public JdbcTemplate hiveMetatoreJdbcTemplate(@Qualifier("hiveMetastoreDataSource") DataSource hiveMetastoreDataSource) {
        return new JdbcTemplate(hiveMetastoreDataSource);
    }


    @Bean(name = "hiveService")
    public HiveService hiveService() {
        return new HiveService();
    }


    @Bean(name = "hiveDataSource")
    public DataSource dataSource() {
        RefreshableDataSource ds = new RefreshableDataSource(kerberosHiveConfiguration,
                                                             usernameCaseStrategyUtil,
                                                             env, "hive.datasource");
        return ds;
    }


    @Bean(name = "hiveMetastoreDataSource")
    @ConfigurationProperties(prefix = "hive.metastore.datasource")
    public DataSource metadataDataSource() {
        DataSource ds = DataSourceBuilder.create().build();
        return ds;
    }

}
