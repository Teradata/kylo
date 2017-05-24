package com.thinkbiganalytics.datalake.authorization.config;

/*-
 * #%L
 * thinkbig-hadoop-authorization-sentry
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

import com.thinkbiganalytics.datalake.authorization.SentryAuthorizationService;
import com.thinkbiganalytics.datalake.authorization.service.HadoopAuthorizationService;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

/**
 */
@Configuration
@PropertySource("classpath:authorization.sentry.properties")
public class SentryConfiguration {

    private static Logger log = LoggerFactory.getLogger(SentryConfiguration.class);

    @Bean(name = "hadoopAuthorizationService")
    public HadoopAuthorizationService getAuthorizationService(@Value("${beeline.connection.url}") String connectionURL
        , @Value("${beeline.drive.name}") String driverURL
        , @Value("${beeline.userName}") String userName
        , @Value("${beeline.password}") String password
        , @Value("${hdfs.hadoop.configuration}") String hadoopConfiguration
        , @Value("${authorization.sentry.groups}") String sentryGroups
        , @Value("${sentry.kerberos.principal}") String kerberosPrincipal
        , @Value("${sentry.kerberos.KeytabLocation}") String kerberosKeytabLocation
        , @Value("${sentry.IsKerberosEnabled}") String kerberosEnabled) {
        SentryConnection sentryConnection = new SentryConnection();
        sentryConnection.setDriverName(driverURL);
        sentryConnection.setSentryGroups(sentryGroups);
        sentryConnection.setHadoopConfiguration(hadoopConfiguration);
        sentryConnection.setDataSource(dataSource(connectionURL, driverURL, userName, password));
        sentryConnection.setKerberosTicketConfiguration(createKerberosTicketConfiguration(kerberosEnabled, hadoopConfiguration, kerberosPrincipal, kerberosKeytabLocation));

        SentryAuthorizationService hadoopAuthorizationService = new SentryAuthorizationService();
        hadoopAuthorizationService.initialize(sentryConnection);
        return hadoopAuthorizationService;
    }

    public DataSource dataSource(String connectionURL
        , String driverURL
        , String userName
        , String password) {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(connectionURL);
        dataSourceBuilder.username(userName);
        dataSourceBuilder.password(password);
        return dataSourceBuilder.build();
    }

    private KerberosTicketConfiguration createKerberosTicketConfiguration(String kerberosEnabled, String hadoopConfigurationResources, String kerberosPrincipal, String keytabLocation) {
        KerberosTicketConfiguration config = new KerberosTicketConfiguration();
        config.setKerberosEnabled("true".equalsIgnoreCase(kerberosEnabled) ? true : false);
        config.setHadoopConfigurationResources(hadoopConfigurationResources);
        config.setKerberosPrincipal(kerberosPrincipal);
        config.setKeytabLocation(keytabLocation);
        return config;
    }

}
