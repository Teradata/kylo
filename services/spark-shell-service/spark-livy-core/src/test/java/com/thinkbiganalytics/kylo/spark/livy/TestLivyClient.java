package com.thinkbiganalytics.kylo.spark.livy;

/*-
 * #%L
 * kylo-spark-livy-core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.kylo.spark.client.model.LivyServer;
import com.thinkbiganalytics.kylo.spark.client.model.enums.LivyServerStatus;
import com.thinkbiganalytics.kylo.spark.config.SparkLivyConfig;
import com.thinkbiganalytics.kylo.spark.model.enums.SessionState;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestLivyClient.Config.class, SparkLivyConfig.class},
                      loader = AnnotationConfigContextLoader.class)
@TestPropertySource
@ActiveProfiles("kylo-livy")
public class TestLivyClient {

    private static final Logger logger = LoggerFactory.getLogger(TestLivyClient.class);

    @Resource
    private LivyServer livyServer;

    @Resource
    private SparkLivyRestClient livyRestProvider;

    /**
     * This test assumes: 1) you have configured a Livy Server and it is running at 8998
     */
    @Test
    public void testHearbeat() {
        // 1. start a session...   through startLivySession.startLivySession, which will now delegate to LivyClient

        // 2. Loop for max number seconds waiting for session to be ready..
        //     only checking livyServerState
    }

    @Configuration
    @EnableConfigurationProperties
    static class Config {

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }
}
