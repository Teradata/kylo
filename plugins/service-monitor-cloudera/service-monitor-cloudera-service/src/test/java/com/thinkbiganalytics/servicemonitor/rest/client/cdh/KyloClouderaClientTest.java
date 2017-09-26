package com.thinkbiganalytics.servicemonitor.rest.client.cdh;

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

import com.cloudera.api.ClouderaManagerClientBuilder;
import com.thinkbiganalytics.servicemonitor.rest.client.RestClientConfig;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Method;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {KyloClouderaClientTest.KyloClouderaHttpsClientTestConfig.class})
public class KyloClouderaClientTest {

    private static final Logger log = LoggerFactory.getLogger(ClouderaClient.class);

    @Autowired
    ClouderaClient clouderaClient;

    @Test
    public void testHttps() {
        try {
            Method generateAddress = ClouderaManagerClientBuilder.class.getDeclaredMethod("generateAddress");
            generateAddress.setAccessible(true);
            ClouderaManagerClientBuilder clouderaManagerClientBuilder = clouderaClient.getClouderaManagerClientBuilder();
            String address = (String) generateAddress.invoke(clouderaManagerClientBuilder);
            Assert.assertEquals(address, "https://cloudera:7183/api/");
        } catch (Exception e) {
            Assert.fail("No protected method [String ClouderaManagerClientBuilder.generateAddress()] available");
        }
    }

    @Configuration
    @ComponentScan(basePackages = {"com.thinkbiganalytics.servicemonitor.rest.client.cdh"})
    public static class KyloClouderaHttpsClientTestConfig {

        @Bean
        public RestClientConfig clouderaRestClientConfig() {
            RestClientConfig restClientConfig = new RestClientConfig();
            restClientConfig.setServerUrl("cloudera");
            restClientConfig.setPort("7183");
            restClientConfig.setUsername("cloudera");
            restClientConfig.setPassword("cloudera");
            restClientConfig.setEnableTLS(Boolean.TRUE);
            return restClientConfig;
        }
    }
}
