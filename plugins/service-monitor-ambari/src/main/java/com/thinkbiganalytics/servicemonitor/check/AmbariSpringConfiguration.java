package com.thinkbiganalytics.servicemonitor.check;

/*-
 * #%L
 * thinkbig-service-monitor-ambari
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

import com.thinkbiganalytics.security.core.encrypt.EncryptionService;
import com.thinkbiganalytics.servicemonitor.rest.client.ambari.AmbariClient;
import com.thinkbiganalytics.servicemonitor.rest.client.ambari.AmbariJerseyClient;
import com.thinkbiganalytics.servicemonitor.rest.client.ambari.AmbariJerseyRestClientConfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

/**
 * Spring configuration
 */
@Configuration
@PropertySource("classpath:ambari.properties")
public class AmbariSpringConfiguration {

    @Inject
    private EncryptionService encryptionService;

    @Bean(name = "ambariServicesStatus")
    public AmbariServicesStatusCheck ambariServicesStatus() {
        return new AmbariServicesStatusCheck();
    }

    @Bean(name = "ambariJerseyClientConfig")
    @ConfigurationProperties(prefix = "ambariRestClientConfig")
    public AmbariJerseyRestClientConfig ambariJerseyRestClientConfig() {
        return new AmbariJerseyRestClientConfig(encryptionService);
    }

    @Bean(name = "ambariClient")
    public AmbariClient ambariClient() {
        return new AmbariJerseyClient(ambariJerseyRestClientConfig());
    }
}
