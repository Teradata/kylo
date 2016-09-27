/**
 * 
 */
package com.thinkbiganalytics.auth.file;

import org.jboss.security.auth.spi.UsersRolesLoginModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;
import com.thinkbiganalytics.auth.jaas.config.JaasAuthConfig;

/**
 * Configures a file-based login module.
 * @author Sean Felten
 */
@Configuration
@Profile("auth-file")
public class FileAuthConfig {
    
    @Value("${security.auth.file.login.ui:required}")
    private String uiLoginFlag;
    
    @Value("${security.auth.file.login.services:required}")
    private String servicesLoginFlag;

    @Bean(name = "servicesFileLoginConfiguration" )
    public LoginConfiguration servicesFileLoginConfiguration(LoginConfigurationBuilder builder) {
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
    }
    
    @Bean(name = "uiFileLoginConfiguration")
    public LoginConfiguration uiFileLoginConfiguration(LoginConfigurationBuilder builder) {
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
    }

}
