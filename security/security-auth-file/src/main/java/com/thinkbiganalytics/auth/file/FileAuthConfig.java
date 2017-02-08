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

/**
 * Configures a file-based login module.
 */
@Configuration
@Profile("auth-file")
public class FileAuthConfig {

    @Value("${security.auth.file.login.ui:required}")
    private String uiLoginFlag;

    @Value("${security.auth.file.login.services:required}")
    private String servicesLoginFlag;

    @Bean(name = "servicesFileLoginConfiguration")
    public LoginConfiguration servicesFileLoginConfiguration(LoginConfigurationBuilder builder) {
        // @formatter:off

        return builder
                        .loginModule(JaasAuthConfig.JAAS_SERVICES)
                            .moduleClass(UsersRolesLoginModule.class)
                            .controlFlag(this.servicesLoginFlag)      
                            .option("defaultUsersProperties", "users.default.properties")
                            .option("defaultRolesProperties", "roles.default.properties")
                            .option("usersProperties", "users.properties")
                            .option("rolesProperties", "roles.properties")
                            .add()
                        .build();

        // @formatter:on
    }

    @Bean(name = "uiFileLoginConfiguration")
    public LoginConfiguration uiFileLoginConfiguration(LoginConfigurationBuilder builder) {
        // @formatter:off

        return builder
                        .loginModule(JaasAuthConfig.JAAS_UI)
                            .moduleClass(UsersRolesLoginModule.class)
                            .controlFlag(this.uiLoginFlag)
                            .option("defaultUsersProperties", "users.default.properties")
                            .option("defaultRolesProperties", "roles.default.properties")
                            .option("usersProperties", "users.properties")
                            .option("rolesProperties", "roles.properties")
                            .add()
                        .build();

        // @formatter:on
    }

}
