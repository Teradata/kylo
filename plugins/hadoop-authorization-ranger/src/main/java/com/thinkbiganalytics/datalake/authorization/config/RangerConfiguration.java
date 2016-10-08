package com.thinkbiganalytics.datalake.authorization.config;

import com.thinkbiganalytics.datalake.authorization.HadoopAuthorizationService;
import com.thinkbiganalytics.datalake.authorization.RangerAuthorizationService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by Jeremy Merrifield on 9/9/16.
 */
@Configuration
@PropertySource("classpath:/conf/authorization.ranger.properties")
public class RangerConfiguration {

    @Bean(name = "hadoopAuthorizationService")
    public HadoopAuthorizationService getAuthorizationService(@Value("${ranger.hostName}") String hostName
        , @Value("${ranger.port}") int port
        , @Value("${ranger.userName}") String userName
        , @Value("${ranger.password}") String password 
        ,@Value ("${hdfs.repository.name}") String hdfs_repository_name
        ,@Value ("${hive.repository.name}") String hive_repository_name) {
        RangerConnection rangerConnection = new RangerConnection();
        rangerConnection.setHostName(hostName);
        rangerConnection.setPort(port);
        rangerConnection.setUsername(userName);
        rangerConnection.setPassword(password);
        rangerConnection.setHdfs_repository_name(hdfs_repository_name);
        rangerConnection.setHive_repository_name(hive_repository_name);
        RangerAuthorizationService hadoopAuthorizationService = new RangerAuthorizationService();
        hadoopAuthorizationService.initialize(rangerConnection);
        return hadoopAuthorizationService;
    }

}
