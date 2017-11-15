/**
 *
 */
package com.thinkbiganalytics.auth.file;

/*-
 * #%L
 * thinkbig-security-auth-file
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

import org.jboss.security.auth.spi.UsersRolesLoginModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.security.auth.login.AppConfigurationEntry;

/**
 * Configures a file-based login module.
 */
@Configuration
@Profile("auth-file")
public class FileAuthConfig {

    @Value("${security.auth.file.login.flag:required}")
    private String loginFlag;
    
    @Value("${security.auth.file.login.order:#{T(com.thinkbiganalytics.auth.jaas.LoginConfiguration).DEFAULT_ORDER}}")
    private int loginOrder;
    
    @Value("${security.auth.file.users:users.properties}")
    private String usersResource;
    
    @Value("${security.auth.file.groups:groups.properties}")
    private String groupsResource;

    @Value("${security.auth.file.password.hash.enabled:false}")
    private boolean passwordHashEnabled;

    @Value("${security.auth.file.password.hash.algorithm:MD5}")
    private String hashAlgorithm;

    @Value("${security.auth.file.password.hash.encoding:base64}")
    private String hashEncoding;

    @Bean(name = "servicesFileLoginConfiguration")
    public LoginConfiguration servicesFileLoginConfiguration(LoginConfigurationBuilder builder) {
        // @formatter:off

        LoginConfigurationBuilder.ModuleBuilder building = builder
                .order(this.loginOrder)
                .loginModule(JaasAuthConfig.JAAS_SERVICES)
                    .moduleClass(FailFastUsersRolesLoginModule.class)
                    .controlFlag(this.loginFlag)
                    .option("defaultUsersProperties", "users.default.properties")
                    .option("defaultRolesProperties", "groups.default.properties")
                    .option("usersProperties", usersResource)
                    .option("rolesProperties", groupsResource);

        if (passwordHashEnabled) {
            building.option("hashAlgorithm", hashAlgorithm)
                    .option("hashEncoding", hashEncoding);
        }

        LoginConfiguration config = building.add().build();
        testConfiguration(config.getAllApplicationEntries().get(JaasAuthConfig.JAAS_SERVICES)[0]);
        return config;

        // @formatter:on
    }

    @Bean(name = "uiFileLoginConfiguration")
    public LoginConfiguration uiFileLoginConfiguration(LoginConfigurationBuilder builder) {
        // @formatter:off

        LoginConfigurationBuilder.ModuleBuilder building = builder
                .order(this.loginOrder)
                .loginModule(JaasAuthConfig.JAAS_UI)
                    .moduleClass(FailFastUsersRolesLoginModule.class)
                    .controlFlag(this.loginFlag)
                    .option("defaultUsersProperties", "users.default.properties")
                    .option("defaultRolesProperties", "groups.default.properties")
                    .option("usersProperties", usersResource)
                    .option("rolesProperties", groupsResource);

        if (passwordHashEnabled) {
            building.option("hashAlgorithm", hashAlgorithm)
                    .option("hashEncoding", hashEncoding);
        }

        LoginConfiguration config = building.add().build();
        testConfiguration(config.getAllApplicationEntries().get(JaasAuthConfig.JAAS_UI)[0]);
        return config;

        // @formatter:on
    }

    /**
     * Test configuration and fail if incorrect, otherwise only fails on login and configuration exception is
     * is converted to Authentication exception without detailed message
     */
    private void testConfiguration(AppConfigurationEntry entry) {
        FailFastUsersRolesLoginModule module = new FailFastUsersRolesLoginModule();
        module.initialize(null, null, null, entry.getOptions());
    }

}
