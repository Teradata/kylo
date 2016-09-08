package com.thinkbiganalytics.auth.db;

import com.thinkbiganalytics.auth.jaas.JaasAuthConfig;
import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.user.UserProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.security.auth.login.AppConfigurationEntry;

/**
 * Spring configuration for the Metadata Login Module.
 */
@Configuration
@Profile("auth-user")
public class KyloAuthConfig {

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    PasswordEncoder passwordEncoder;

    @Inject
    UserProvider userProvider;

    /**
     * Creates a new services login configuration using the Metadata Login Module.
     *
     * @param builder the login configuration builder
     * @return the services login configuration
     */
    @Bean(name = "servicesDatabaseLoginConfiguration")
    @Nonnull
    public LoginConfiguration servicesMetadataLoginConfiguration(@Nonnull final LoginConfigurationBuilder builder) {
        return builder
                .loginModule(JaasAuthConfig.JAAS_SERVICES)
                    .moduleClass(KyloLoginModule.class)
                    .controlFlag(AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL)
                    .option(KyloLoginModule.METADATA_ACCESS, metadataAccess)
                    .option(KyloLoginModule.PASSWORD_ENCODER, passwordEncoder)
                    .option(KyloLoginModule.USER_PROVIDER, userProvider)
                    .add()
                .build();
    }

    /**
     * Creates a new UI login configuration using the Metadata Login Module.
     *
     * @param builder the login configuration builder
     * @return the UI login configuration
     */
    @Bean(name = "uiDatabaseLoginConfiguration")
    @Nonnull
    public LoginConfiguration uiMetadataLoginConfiguration(@Nonnull final LoginConfigurationBuilder builder) {
        return builder
                .loginModule(JaasAuthConfig.JAAS_UI)
                    .moduleClass(KyloLoginModule.class)
                    .controlFlag(AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL)
                    .option(KyloLoginModule.METADATA_ACCESS, metadataAccess)
                    .option(KyloLoginModule.PASSWORD_ENCODER, passwordEncoder)
                    .option(KyloLoginModule.USER_PROVIDER, userProvider)
                    .add()
                .build();
    }
}
