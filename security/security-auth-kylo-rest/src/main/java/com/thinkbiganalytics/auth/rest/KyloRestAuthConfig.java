package com.thinkbiganalytics.auth.rest;

/*-
 * #%L
 * REST API Authentication
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

import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;
import com.thinkbiganalytics.auth.jaas.config.JaasAuthConfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Spring configuration for the Kylo REST API Login Module.
 */
@Configuration
@Profile("auth-kylo")
public class KyloRestAuthConfig {

    @Value("${security.auth.kylo.login.flag:required}")
    private String loginFlag;
    
    @Value("${security.auth.kylo.login.order:#{T(com.thinkbiganalytics.auth.jaas.LoginConfiguration).DEFAULT_ORDER}}")
    private int loginOrder;

    @Bean(name = "loginRestClientConfig")
    @ConfigurationProperties(prefix = "loginRestClientConfig")
    public LoginJerseyClientConfig loginRestClientConfig() {
        return new LoginJerseyClientConfig();
    }

    @Inject
    private LoginJerseyClientConfig loginRestClientConfig;

    @Value("${security.auth.kylo.login.username:#{null}}")
    private String loginUser;

    @Value("${security.auth.kylo.login.password:#{null}}")
    private String loginPassword;

    /**
     * Creates a new UI login configuration for the REST Login Module.
     *
     * @param builder the login configuration builder
     * @return the UI login configuration
     */
    @Bean(name = "kyloRestLoginConfiguration")
    @Nonnull
    public LoginConfiguration servicesRestLoginConfiguration(@Nonnull final LoginConfigurationBuilder builder) {
        // @formatter:off

        return builder
                .order(this.loginOrder)
                .loginModule(JaasAuthConfig.JAAS_UI)
                    .moduleClass(KyloRestLoginModule.class)
                    .controlFlag(this.loginFlag)
                    .option(KyloRestLoginModule.REST_CLIENT_CONFIG, loginRestClientConfig)
                    .add()
                .loginModule(JaasAuthConfig.JAAS_UI_TOKEN)
                    .moduleClass(KyloRestLoginModule.class)
                    .controlFlag(this.loginFlag)
                    .option(KyloRestLoginModule.REST_CLIENT_CONFIG, loginRestClientConfig)
                    .option(KyloRestLoginModule.LOGIN_USER, loginUser)
                    .option(KyloRestLoginModule.LOGIN_PASSWORD, loginPassword)
                    .add()
                .build();

        // @formatter:on
    }
}
