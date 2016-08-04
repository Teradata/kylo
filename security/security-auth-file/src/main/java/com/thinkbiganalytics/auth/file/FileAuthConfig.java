/**
 * 
 */
package com.thinkbiganalytics.auth.file;

import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.jboss.security.auth.spi.UsersRolesLoginModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.thinkbiganalytics.auth.jaas.JaasAuthConfig;
import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;

/**
 * Configures a file-based login module.
 * @author Sean Felten
 */
@Configuration
@Profile("auth-file")
public class FileAuthConfig {
    
    @Bean(name = "servicesFileLoginConfiguration" )
    public LoginConfiguration servicesFileLoginConfiguration(LoginConfigurationBuilder builder) {
        return builder
                        .loginModule(JaasAuthConfig.JAAS_SERVICES)
                            .moduleClass(UsersRolesLoginModule.class)
                            .controlFlag(LoginModuleControlFlag.OPTIONAL)      
                            .option("defaultUsersProperties", "users.default.properties")
                            .option("defaultRolesProperties", "roles.default.properties")
                            .option("users.properties", "users.properties")
                            .option("rolesProperties", "roles.properties")
                            .add()
                        .build();
    }
    
    @Bean(name = "uiFileLoginConfiguration")
    public LoginConfiguration uiFileLoginConfiguration(LoginConfigurationBuilder builder) {
        return builder
                        .loginModule(JaasAuthConfig.JAAS_UI)
                            .moduleClass(UsersRolesLoginModule.class)
                            .controlFlag(LoginModuleControlFlag.OPTIONAL)
                            .option("defaultUsersProperties", "users.default.properties")
                            .option("defaultRolesProperties", "roles.default.properties")
                            .option("users.properties", "users.properties")
                            .option("rolesProperties", "roles.properties")
                            .add()
                        .build();
    }

}
