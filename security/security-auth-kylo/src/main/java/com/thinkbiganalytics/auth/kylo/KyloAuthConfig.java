package com.thinkbiganalytics.auth.kylo;

import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.security.auth.login.AppConfigurationEntry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;
import com.thinkbiganalytics.auth.jaas.config.JaasAuthConfig;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserGroup;
import com.thinkbiganalytics.metadata.api.user.UserProvider;

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
        return () -> {
            metadata.commit(() -> {
                Optional<User> dlOption = this.userProvider.findUserBySystemName("dladmin");
                User dladmin = null;
                
                if (dlOption.isPresent()) {
                    dladmin = dlOption.get();
                } else {
                    dladmin = this.userProvider.ensureUser("dladmin");
                    dladmin.setPassword(this.passwordEncoder.encode("thinkbig"));
                    dladmin.setDisplayName("Data Lake Administrator");
                }
                
                this.userProvider.ensureGroup("user");
                this.userProvider.ensureGroup("operations");
                this.userProvider.ensureGroup("designer");
                this.userProvider.ensureGroup("analyst");
                
                UserGroup adminGroup = this.userProvider.ensureGroup("admin");
                adminGroup.addUser(dladmin);
            }, MetadataAccess.SERVICE);
        };
    }
}
