package com.thinkbiganalytics.datalake.authorization.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.thinkbiganalytics.datalake.authorization.SentryAuthorizationService;
import com.thinkbiganalytics.datalake.authorization.service.HadoopAuthorizationService;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;

/**
 * Created by Shashi Vishwakarma on 20/9/16.
 */
@Configuration
@PropertySource("classpath:/conf/authorization.sentry.properties")
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
        , @Value ("${sentry.kerberos.KeytabLocation}") String kerberosKeytabLocation
        , @Value ("${sentry.IsKerberosEnabled}") String kerberosEnabled) {
        SentryConnection sentryConnection = new SentryConnection();
        sentryConnection.setDriverName(driverURL);
        sentryConnection.setSentryGroups(sentryGroups);
        sentryConnection.setHadoopConfiguration(hadoopConfiguration);
        sentryConnection.setDataSource(dataSource(connectionURL, driverURL, userName, password));
        sentryConnection.setKerberosTicketConfiguration(createKerberosTicketConfiguration(kerberosEnabled,hadoopConfiguration,kerberosPrincipal,kerberosKeytabLocation));
     
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
    
    private KerberosTicketConfiguration createKerberosTicketConfiguration(String kerberosEnabled, String hadoopConfigurationResources, String kerberosPrincipal, String keytabLocation ) {
        KerberosTicketConfiguration config = new KerberosTicketConfiguration();
        config.setKerberosEnabled("true".equalsIgnoreCase(kerberosEnabled) ? true: false);
        config.setHadoopConfigurationResources(hadoopConfigurationResources);
        config.setKerberosPrincipal(kerberosPrincipal);
        config.setKeytabLocation(keytabLocation);
        return config;
    }

}
