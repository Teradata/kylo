package com.thinkbiganalytics.datalake.authorization.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.thinkbiganalytics.datalake.authorization.HadoopAuthorizationService;
import com.thinkbiganalytics.datalake.authorization.SentryAuthorizationService;

/**
 * Created by Shashi Vishwakarma on 20/9/16.
 */
@Configuration
@PropertySource("classpath:/conf/authorization.sentry.properties")
public class SentryConfiguration {

    @Bean(name = "hadoopAuthorizationService")
    public HadoopAuthorizationService getAuthorizationService(@Value("${beeline.connection.url}") String connectionURL
        , @Value("${beeline.drive.name}") String  driverURL
        , @Value("${beeline.userName}") String userName
        , @Value("${beeline.password}") String password) {
        SentryConnection sentryConnection = new SentryConnection();
        sentryConnection.setConnectionURL(connectionURL);
        sentryConnection.setDriverName(driverURL);
        sentryConnection.setUsername(userName);
        sentryConnection.setPassword(password);
        SentryAuthorizationService hadoopAuthorizationService = new SentryAuthorizationService();
        hadoopAuthorizationService.initialize(sentryConnection);
        return hadoopAuthorizationService;
    }

}
