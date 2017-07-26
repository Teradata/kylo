/**
 * 
 */
package com.thinkbiganalytics.auth.jaas.config;

/*-
 * #%L
 * kylo-security-auth
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.security.auth.login.AppConfigurationEntry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.thinkbiganalytics.auth.jaas.AbstractLoginModule;
import com.thinkbiganalytics.auth.jaas.config.DefaultLoginConfigurationBuilderTest.TestConfig;
import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { JaasAuthConfig.class, TestConfig.class })
public class DefaultLoginConfigurationBuilderTest {
    
    @Configuration
    public static class TestConfig {
        @Bean
        public LoginConfiguration loginConfiguration1(LoginConfigurationBuilder builder) {
            return builder
                .order(LoginConfiguration.DEFAULT_ORDER)
                .loginModule(JaasAuthConfig.JAAS_SERVICES)
                    .moduleClass(TestLoginModule.class)
                    .option("name", "1")
                    .add()
                .loginModule(JaasAuthConfig.JAAS_SERVICES)
                    .moduleClass(TestLoginModule.class)
                    .option("name", "4")
                    .add()
                .build();
        }
        
        @Bean
        @Order(2)
        public LoginConfiguration loginConfiguration2(LoginConfigurationBuilder builder) {
            return builder
                .order(LoginConfiguration.DEFAULT_ORDER + 1)
                .loginModule(JaasAuthConfig.JAAS_SERVICES)
                    .moduleClass(TestLoginModule.class)
                    .option("name", "2")
                    .add()
                .loginModule(JaasAuthConfig.JAAS_SERVICES)
                    .moduleClass(TestLoginModule.class)
                    .option("name", "3")
                    .add()
                .build();
        }
    }
    
    @Inject
    private javax.security.auth.login.Configuration configuration;
    
    @Test
    public void testOrder() {
        AppConfigurationEntry[] entries = this.configuration.getAppConfigurationEntry(JaasAuthConfig.JAAS_SERVICES);
        List<String> names = Arrays.stream(entries)
                        .map(e -> e.getOptions().get("name"))
                        .map(String.class::cast)
                        .collect(Collectors.toList());
        assertThat(names).containsExactly("1", "4", "2", "3");
    }
    
    
    public static class TestLoginModule extends AbstractLoginModule {
        @Override
        protected boolean doLogin() throws Exception {
            return true;
        }

        @Override
        protected boolean doCommit() throws Exception {
            return true;
        }

        @Override
        protected boolean doAbort() throws Exception {
            return true;
        }

        @Override
        protected boolean doLogout() throws Exception {
            return true;
        }
    }
}
