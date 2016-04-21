package com.thinkbiganalytics.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;

/**
 * Generic Auth Service Configuration
 * To Override create a new Configuration Class importing this class
 * Example:
 * @Configuration
 * @Import(AuthConfig.class)
 * public class MyAuthConfig {
 *     @Bean(name = "authenticationService")
       public  AuthenticationService authenticationService(){
        return new MyAuthService();
        }
 * }
 */
@Configuration
public class AuthConfig {

    @Bean(name = "authenticationService")
    public AuthenticationService authenticationService() {
        return new SimpleAuthenticationService();
    }

    @Bean(name = "authenticationProvider")
    public AuthenticationProvider authenticationProvider() {
        return new AuthServiceAuthenticationProvider();
    }
}
