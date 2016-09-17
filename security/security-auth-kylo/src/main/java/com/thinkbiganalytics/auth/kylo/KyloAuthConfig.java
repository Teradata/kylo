package com.thinkbiganalytics.auth.kylo;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.security.auth.login.AppConfigurationEntry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

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

/**
 * Spring configuration for the Metadata Login Module.
 */
@Configuration
@Profile("auth-kylo")
public class KyloAuthConfig {

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
     * Creates a new services login configuration using the Metadata Login Module.
     *
     * @param builder the login configuration builder
     * @return the services login configuration
     */
    @Bean(name = "servicesKyloLoginConfiguration")
    @Nonnull
    public LoginConfiguration servicesKyloLoginConfiguration(@Nonnull final LoginConfigurationBuilder builder) {
        return builder
                .loginModule(JaasAuthConfig.JAAS_SERVICES)
                    .moduleClass(KyloLoginModule.class)
                    .controlFlag(AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL)
                    .option(KyloLoginModule.METADATA_ACCESS, metadataAccess)
                    .option(KyloLoginModule.PASSWORD_ENCODER, passwordEncoder)
                    .option(KyloLoginModule.USER_PROVIDER, userProvider)
                    .option(KyloLoginModule.REQUIRE_PASSWORD, this.authPassword)
                    .add()
                .build();
    }

    /**
     * Creates a new UI login configuration using the Kylo Login Module.
     *
     * @param builder the login configuration builder
     * @return the UI login configuration
     */
    @Bean(name = "uiKyloLoginConfiguration")
    @Nonnull
    public LoginConfiguration uiKyloLoginConfiguration(@Nonnull final LoginConfigurationBuilder builder) {
        return builder
                .loginModule(JaasAuthConfig.JAAS_UI)
                    .moduleClass(KyloLoginModule.class)
                    .controlFlag(AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL)
                    .option(KyloLoginModule.METADATA_ACCESS, metadataAccess)
                    .option(KyloLoginModule.PASSWORD_ENCODER, passwordEncoder)
                    .option(KyloLoginModule.USER_PROVIDER, userProvider)
                    .option(KyloLoginModule.REQUIRE_PASSWORD, this.authPassword)
                    .add()
                .build();
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
