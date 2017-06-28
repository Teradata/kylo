package com.thinkbiganalytics.auth.kylo;

/*-
 * #%L
 * UserProvider Authentication
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
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserGroup;
import com.thinkbiganalytics.metadata.api.user.UserProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Spring configuration for the Metadata Login Module.
 */
@Configuration
@Profile("auth-kylo")
public class KyloAuthConfig {

    @Value("${security.auth.kylo.login.flag:required}")
    private String loginFlag;
    
    @Value("${security.auth.kylo.login.order:#{T(com.thinkbiganalytics.auth.jaas.LoginConfiguration).DEFAULT_ORDER}}")
    private int loginOrder;

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private UserProvider userProvider;

    @Value("${security.auth.kylo.password.required:false}")
    private boolean authPassword;

    /**
     * Creates a new services login configuration using the Metadata Login Module.  Currently
     * this LoginModule is only applicable on the services side.
     *
     * @param builder the login configuration builder
     * @return the services login configuration
     */
    @Bean(name = "kyloLoginConfiguration")
    @Nonnull
    public LoginConfiguration servicesKyloLoginConfiguration(@Nonnull final LoginConfigurationBuilder builder) {
        // @formatter:off

        return builder
                .order(this.loginOrder)
                .loginModule(JaasAuthConfig.JAAS_SERVICES)
                    .moduleClass(KyloLoginModule.class)
                    .controlFlag(this.loginFlag)
                    .option(KyloLoginModule.METADATA_ACCESS, metadataAccess)
                    .option(KyloLoginModule.PASSWORD_ENCODER, passwordEncoder)
                    .option(KyloLoginModule.USER_PROVIDER, userProvider)
                    .option(KyloLoginModule.REQUIRE_PASSWORD, this.authPassword)
                    .add()
                .loginModule(JaasAuthConfig.JAAS_SERVICES_TOKEN)
                    .moduleClass(KyloLoginModule.class)
                    .controlFlag(this.loginFlag)
                    .option(KyloLoginModule.METADATA_ACCESS, metadataAccess)
                    .option(KyloLoginModule.PASSWORD_ENCODER, passwordEncoder)
                    .option(KyloLoginModule.USER_PROVIDER, userProvider)
                    .option(KyloLoginModule.REQUIRE_PASSWORD, this.authPassword)
                    .add()
                .build();

        // @formatter:on
    }
    
    protected User createDefaultUser(String username, String displayName) {
        Optional<User> userOption = userProvider.findUserBySystemName(username);
        User user = null;
        
        // Create the user if it doesn't exists.
        if (userOption.isPresent()) {
            user = userOption.get();
        } else {
            user = userProvider.ensureUser(username);
            user.setPassword(passwordEncoder.encode(username));
            user.setDisplayName(displayName);
        }
        
        return user;
    }

    protected UserGroup createDefaultGroup(String groupName, String title, String oldGroupName) {
        UserGroup newGroup = userProvider.ensureGroup(groupName);
        newGroup.setTitle(title);
        
        // If there is an old group replacing this new group transfer the users before deleting it.
        if (oldGroupName != null) {
            userProvider.findGroupByName(oldGroupName).ifPresent(oldGrp -> { 
                oldGrp.getUsers().forEach(user -> newGroup.addUser(user));
                userProvider.deleteGroup(oldGrp);
            });
        }
        
        return newGroup;
    }
}
