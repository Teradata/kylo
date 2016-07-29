package com.thinkbiganalytics.auth;

import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.thinkbiganalytics.auth.jaas.JaasAuthConfig;
import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;

/**
 * Generic Auth Service Configuration for installing a login module to authenticate using an AuthenticationService.
 * To Override create a new Configuration Class importing this class
 * Example:
 * @Configuration
 * @Import(SimpleAuthConfig.class)
 * public class MyAuthConfig {
 *     @Bean(name = "authenticationService")
       public  AuthenticationService authenticationService(){
        return new MyAuthService();
        }
 * }
 */
@Configuration
@Profile("simple-auth")
public class SimpleAuthConfig {
    
    @Bean(name = "authenticationService")
    public AuthenticationService authenticationService() {
        return new SimpleAuthenticationService();
    }
    
    @Bean(name = "restServiceLoginConfiguration")
    public LoginConfiguration restServiceLoginConfiguration(LoginConfigurationBuilder builder) {
        return builder
                        .loginModule(JaasAuthConfig.JAAS_REST)
                            .moduleClass(AuthServiceLoginModule.class)
                            .controlFlag(LoginModuleControlFlag.REQUIRED)
                            .option("authService", authenticationService())
                            .add()
                        .build();
    }
    
    @Bean(name = "uiServiceLoginConfiguration")
    public LoginConfiguration uiServiceLoginConfiguration(LoginConfigurationBuilder builder) {
        return builder
                        .loginModule(JaasAuthConfig.JAAS_UI)
                            .moduleClass(AuthServiceLoginModule.class)
                            .controlFlag(LoginModuleControlFlag.REQUIRED)
                            .option("authService", authenticationService())
                            .add()
                        .build();
    }
}
