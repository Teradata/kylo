package com.thinkbiganalytics.auth.metadata;

import com.thinkbiganalytics.auth.jaas.JaasAuthConfig;
import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.Nonnull;
import javax.security.auth.login.AppConfigurationEntry;

/**
 * Spring configuration for the Metadata Login Module.
 */
@Configuration
@Profile("auth-metadata")
public class MetadataAuthConfig {

    /**
     * Creates a new services login configuration using the Metadata Login Module.
     *
     * @param builder the login configuration builder
     * @return the services login configuration
     */
    @Bean(name = "servicesMetadataLoginConfiguration")
    @Nonnull
    public LoginConfiguration servicesMetadataLoginConfiguration(@Nonnull final LoginConfigurationBuilder builder) {
        return builder
                .loginModule(JaasAuthConfig.JAAS_SERVICES)
                    .moduleClass(MetadataLoginModule.class)
                    .controlFlag(AppConfigurationEntry.LoginModuleControlFlag.REQUIRED)
                    .add()
                .build();
    }

    /**
     * Creates a new UI login configuration using the Metadata Login Module.
     *
     * @param builder the login configuration builder
     * @return the UI login configuration
     */
    @Bean(name = "uiMetadataLoginConfiguration")
    @Nonnull
    public LoginConfiguration uiMetadataLoginConfiguration(@Nonnull final LoginConfigurationBuilder builder) {
        return builder
                .loginModule(JaasAuthConfig.JAAS_UI)
                    .moduleClass(MetadataLoginModule.class)
                    .controlFlag(AppConfigurationEntry.LoginModuleControlFlag.REQUIRED)
                    .add()
                .build();
    }
}
