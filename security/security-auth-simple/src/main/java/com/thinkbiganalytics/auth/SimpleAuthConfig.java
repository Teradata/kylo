package com.thinkbiganalytics.auth;

/*-
 * #%L
 * thinkbig-security-auth-simple
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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

/**
 * Generic Auth Service Configuration for installing a login module to authenticate using an AuthenticationService.
 * To Override create a new Configuration Class importing this class
 * Example:
 *
 * @Configuration
 * @Import(SimpleAuthConfig.class) public class MyAuthConfig {
 * @Bean(name = "authenticationService") public  AuthenticationService authenticationService(){ return new MyAuthService(); } }
 */
@Configuration
@Profile("auth-simple")
public class SimpleAuthConfig {

    @Bean(name = "authenticationService")
    @ConfigurationProperties("authenticationService")
    public AuthenticationService authenticationService() {
        return new SimpleAuthenticationService();
    }

    @Bean(name = "servicesLoginConfiguration")
    public LoginConfiguration servicesLoginConfiguration(LoginConfigurationBuilder builder) {
        // @formatter:off

        return builder
                        .loginModule(JaasAuthConfig.JAAS_SERVICES)
                            .moduleClass(AuthServiceLoginModule.class)
                            .controlFlag(LoginModuleControlFlag.REQUIRED)
                            .option("authService", authenticationService())
                            .add()
                        .build();

        // @formatter:on
    }

    @Bean(name = "uiServiceLoginConfiguration")
    public LoginConfiguration uiServiceLoginConfiguration(LoginConfigurationBuilder builder) {
        // @formatter:off

        return builder
                        .loginModule(JaasAuthConfig.JAAS_UI)
                            .moduleClass(AuthServiceLoginModule.class)
                            .controlFlag(LoginModuleControlFlag.REQUIRED)
                            .option("authService", authenticationService())
                            .add()
                        .build();
        
        // @formatter:on
    }
}
