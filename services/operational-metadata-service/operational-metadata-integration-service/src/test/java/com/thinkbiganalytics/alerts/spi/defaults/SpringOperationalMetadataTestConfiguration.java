package com.thinkbiganalytics.alerts.spi.defaults;
/*-
 * #%L
 * thinkbig-alerts-default
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
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.jms.JmsService;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.servicemonitor.ServiceMonitorRepository;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.ConnectionFactory;

/**
 * Created by sr186054 on 8/10/17.
 */
@Configuration
public class SpringOperationalMetadataTestConfiguration {

    @Bean
    public JmsService jmsService(){
        return Mockito.mock(JmsService.class);
    }

    @Bean
    public ConnectionFactory connectionFactory(){
        return Mockito.mock(ConnectionFactory.class);
    }

    @Bean
    public NifiFlowCache nifiFlowCache(){
        return Mockito.mock(NifiFlowCache.class);
    }

    @Bean
    public NiFiRestClient niFiRestClient(){
        return Mockito.mock(NiFiRestClient.class);
    }

    @Bean
    public NiFiPropertyDescriptorTransform niFiPropertyDescriptorTransform(){
        return Mockito.mock(NiFiPropertyDescriptorTransform.class);
    }



    @Bean
    public AccessController accessController() {
        return Mockito.mock(AccessController.class);
    }


    @Bean
    public ServiceMonitorRepository serviceMonitorRepository() {
        return Mockito.mock(ServiceMonitorRepository.class);
    }



}
