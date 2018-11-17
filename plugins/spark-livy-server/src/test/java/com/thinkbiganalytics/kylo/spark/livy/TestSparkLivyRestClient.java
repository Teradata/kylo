package com.thinkbiganalytics.kylo.spark.livy;

/*-
 * #%L
 * kylo-spark-livy-server
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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.kylo.spark.config.SparkLivyConfig;
import com.thinkbiganalytics.kylo.spark.model.SessionsGet;
import com.thinkbiganalytics.kylo.spark.model.Statement;
import com.thinkbiganalytics.kylo.spark.model.enums.StatementOutputStatus;
import com.thinkbiganalytics.kylo.spark.model.enums.StatementState;
import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.spark.conf.model.KerberosSparkProperties;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.annotation.Resource;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestSparkLivyRestClient.Config.class, SparkLivyConfig.class},
                      loader = AnnotationConfigContextLoader.class)
@TestPropertySource("classpath:kerberos-client.properties")
@ActiveProfiles("kylo-livy")
public class TestSparkLivyRestClient {

    private static final Logger logger = LoggerFactory.getLogger(TestSparkLivyRestClient.class);

    @Resource
    private SparkLivyProcessManager sparkLivyProcessManager;

    @Resource
    private KerberosSparkProperties kerberosSparkProperties;

    @Resource
    private SparkLivyRestClient livyRestProvider;

    /**
     * This test assumes: 1) you have configured a Livy Server for Kerberos and that it is running at 8998 2) that server has the library 'kylo-spark-shell-client-v1-0.10.0-SNAPSHOT.jar' installed
     */
    @Test
    @Ignore // ignore, for now, because this is an integration test that requires livy configured on the test system
    public void testGetLivySessions() throws JsonProcessingException, InterruptedException {
        System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("sun.security.jgss.debug", "true");

        SessionsGet sg = new SessionsGet.Builder().from(1).size(2).build();

        String sgJson = new ObjectMapper().writeValueAsString(sg);

        logger.debug("{}", sgJson);
        assertThat(sgJson).isEqualToIgnoringCase("{\"from\":1,\"size\":2}");

        logger.debug("kerberosSparkProperties={}", kerberosSparkProperties);

        SparkShellProcess sparkProcess = sparkLivyProcessManager.getProcessForUser("kylo");
        //((SparkLivyProcess) sparkProcess).setPort(8999);

        JerseyRestClient client = sparkLivyProcessManager.getClient(sparkProcess);

        sparkLivyProcessManager.start("kylo");

        Integer stmtId = 0;
        Statement statement = livyRestProvider.pollStatement(client, sparkProcess, stmtId);
        logger.debug("statement={}", statement);

        assertThat(statement.getState()).isEqualTo(StatementState.available);
        assertThat(statement.getOutput().getStatus()).isEqualTo(StatementOutputStatus.ok);
    }

    @Configuration
    static class Config {

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }
}
