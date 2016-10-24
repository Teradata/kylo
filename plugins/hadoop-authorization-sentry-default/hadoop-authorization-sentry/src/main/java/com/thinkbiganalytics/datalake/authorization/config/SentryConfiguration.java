package com.thinkbiganalytics.datalake.authorization.config;

import com.thinkbiganalytics.datalake.authorization.SentryAuthorizationService;
import com.thinkbiganalytics.datalake.authorization.service.HadoopAuthorizationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

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
        , @Value("${authorization.sentry.groups}") String sentryGroups) {
        SentryConnection sentryConnection = new SentryConnection();
        sentryConnection.setDriverName(driverURL);
        sentryConnection.setSentryGroups(sentryGroups);
        sentryConnection.setHadoopConfiguration(hadoopConfiguration);
        sentryConnection.setDataSource(dataSource(connectionURL, driverURL, userName, password));

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

}
