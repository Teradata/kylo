package com.thinkbiganalytics.servicemonitor;

/*-
 * #%L
 * thinkbig-service-monitor-cloudera
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

import com.thinkbiganalytics.servicemonitor.check.ClouderaServicesStatusCheck;
import com.thinkbiganalytics.servicemonitor.rest.client.cdh.ClouderaClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Spring configuration for Cloudera service monitor.
 */
@Configuration
@PropertySource("classpath:cloudera.properties")
public class ClouderaSpringConfiguration {

    @Bean(name = "clouderaServicesStatusCheck")
    public ClouderaServicesStatusCheck clouderaServicesStatusCheck() {
        return new ClouderaServicesStatusCheck();
    }

    @Bean
    public ClouderaClient clouderaClient() {
        return new ClouderaClient();
    }

}
