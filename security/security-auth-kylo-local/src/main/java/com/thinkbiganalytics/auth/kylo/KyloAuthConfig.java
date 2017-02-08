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
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.jobrepo.security.OperationsAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserGroup;
import com.thinkbiganalytics.metadata.api.user.UserProvider;
import com.thinkbiganalytics.security.action.AllowedModuleActionsProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
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

    @Value("${security.auth.kylo.login.services:required}")
    private String servicesLoginFlag;

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private UserProvider userProvider;

    @Inject
    private AllowedModuleActionsProvider actionsProvider;

    @Inject
    private MetadataAccess metadata;

    @Value("${auth.kylo.password.required:false}")
    private boolean authPassword;

    /**
     * Creates a new services login configuration using the Metadata Login Module.  Currently
     * this LoginModule is only applicable on the services side.
     *
     * @param builder the login configuration builder
     * @return the services login configuration
     */
    @Bean(name = "servicesKyloLoginConfiguration")
    @Nonnull
    public LoginConfiguration servicesKyloLoginConfiguration(@Nonnull final LoginConfigurationBuilder builder) {
        // @formatter:off

        return builder
                .loginModule(JaasAuthConfig.JAAS_SERVICES)
                    .moduleClass(KyloLoginModule.class)
                    .controlFlag(this.servicesLoginFlag)
                    .option(KyloLoginModule.METADATA_ACCESS, metadataAccess)
                    .option(KyloLoginModule.PASSWORD_ENCODER, passwordEncoder)
                    .option(KyloLoginModule.USER_PROVIDER, userProvider)
                    .option(KyloLoginModule.REQUIRE_PASSWORD, this.authPassword)
                    .add()
                .loginModule(JaasAuthConfig.JAAS_SERVICES_TOKEN)
                    .moduleClass(KyloLoginModule.class)
                    .controlFlag(this.servicesLoginFlag)
                    .option(KyloLoginModule.METADATA_ACCESS, metadataAccess)
                    .option(KyloLoginModule.PASSWORD_ENCODER, passwordEncoder)
                    .option(KyloLoginModule.USER_PROVIDER, userProvider)
                    .option(KyloLoginModule.REQUIRE_PASSWORD, this.authPassword)
                    .add()
                .build();

        // @formatter:on
    }

    @Bean
    public PostMetadataConfigAction addDefaultUsersAction() {
        return new PopulateDefaultKyloEntitiesAction();
    }


    @Order(PostMetadataConfigAction.DEFAULT_ORDER + 100)
    private class PopulateDefaultKyloEntitiesAction implements PostMetadataConfigAction {

        @Override
        public void run() {
            metadata.commit(() -> {
                Optional<User> dlOption = userProvider.findUserBySystemName("dladmin");
                User dladmin = null;

                // Create the dladmin user if it doesn't exists.
                if (dlOption.isPresent()) {
                    dladmin = dlOption.get();
                } else {
                    dladmin = userProvider.ensureUser("dladmin");
                    dladmin.setPassword(passwordEncoder.encode("thinkbig"));
                    dladmin.setDisplayName("Data Lake Administrator");
                }

                // Create default groups if they don't exist.
                UserGroup userGroup = userProvider.ensureGroup("user");
                UserGroup opsGroup = userProvider.ensureGroup("operations");
                UserGroup designerGroup = userProvider.ensureGroup("designer");
                UserGroup analystGroup = userProvider.ensureGroup("analyst");
                UserGroup adminGroup = userProvider.ensureGroup("admin");

                // Add dladmin to admin group
                adminGroup.addUser(dladmin);

                // Setup initial access control.  Admin group already has all rights.
                actionsProvider.getAllowedActions("services")
                    .ifPresent((allowed) -> {
                        allowed.enable(opsGroup.getRootPrincial(),
                                       OperationsAccessControl.ADMIN_OPS,
                                       FeedsAccessControl.ACCESS_CATEGORIES,
                                       FeedsAccessControl.ACCESS_FEEDS);
                        allowed.enable(designerGroup.getRootPrincial(),
                                       OperationsAccessControl.ACCESS_OPS,
                                       FeedsAccessControl.EDIT_FEEDS,
                                       FeedsAccessControl.IMPORT_FEEDS,
                                       FeedsAccessControl.EXPORT_FEEDS,
                                       FeedsAccessControl.EDIT_CATEGORIES,
                                       FeedsAccessControl.EDIT_TEMPLATES);
                        allowed.enable(analystGroup.getRootPrincial(),
                                       OperationsAccessControl.ACCESS_OPS,
                                       FeedsAccessControl.EDIT_FEEDS,
                                       FeedsAccessControl.ACCESS_CATEGORIES,
                                       FeedsAccessControl.IMPORT_TEMPLATES,
                                       FeedsAccessControl.EXPORT_TEMPLATES,
                                       FeedsAccessControl.ACCESS_TEMPLATES);
                    });
            }, MetadataAccess.SERVICE);
        }
    }
}
