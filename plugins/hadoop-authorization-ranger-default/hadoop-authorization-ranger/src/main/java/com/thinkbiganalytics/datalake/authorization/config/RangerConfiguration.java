package com.thinkbiganalytics.datalake.authorization.config;

/*-
 * #%L
 * thinkbig-hadoop-authorization-ranger
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

import com.thinkbiganalytics.datalake.authorization.RangerAuthorizationService;
import com.thinkbiganalytics.datalake.authorization.service.HadoopAuthorizationService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 */
@Configuration
@PropertySource("classpath:authorization.ranger.properties")
public class RangerConfiguration {

    @Bean(name = "hadoopAuthorizationService")
    public HadoopAuthorizationService getAuthorizationService(@Value("${ranger.hostName}") String hostName
        , @Value("${ranger.port}") int port
        , @Value("${ranger.userName}") String userName
        , @Value("${ranger.password}") String password
        , @Value("${hdfs.repository.name}") String hdfsRepositoryName
        , @Value("${hive.repository.name}") String hiveRepositoryName) {
        RangerConnection rangerConnection = new RangerConnection();
        rangerConnection.setHostName(hostName);
        rangerConnection.setPort(port);
        rangerConnection.setUsername(userName);
        rangerConnection.setPassword(password);
        rangerConnection.setHdfsRepositoryName(hdfsRepositoryName);
        rangerConnection.setHiveRepositoryName(hiveRepositoryName);
        RangerAuthorizationService hadoopAuthorizationService = new RangerAuthorizationService();
        hadoopAuthorizationService.initialize(rangerConnection);
        return hadoopAuthorizationService;
    }

}
